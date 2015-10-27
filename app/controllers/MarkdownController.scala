package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import forms.MarkdownForm
import models.ParseOperationInfo._
import models.services.UserService
import models.{ParseOperationInfo, User}
import org.joda.time.DateTime
import play.api.Configuration
import play.api.i18n._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import reactivemongo.api.ReadPreference
import service.MarkdownToHtmlParser
import service.MarkdownToHtmlParser.MarkdownToHtmlParserException

import scala.concurrent.Future

/**
 * The markdown parse controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 */
class MarkdownController @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[User, CookieAuthenticator],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock,
  val reactiveMongoApi: ReactiveMongoApi)
  extends Silhouette[User, CookieAuthenticator] with Controller with MongoController with ReactiveMongoComponents {

  // get the collection 'parseOperations'
  val collection = db[JSONCollection]("parseOperations")

  val exampleTemplate = "" +
    "# huge header\n" +
    "### modest header\n" +
    "plain text\n" +
    "*a little emphasized text*\n" +
    "**very strong text**\n" +
    "[link](http://mysite.com)"

  /**
   * Handles the "Home" action.
   *
   * @return The result to display.
   */
  def index = UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.markdown(MarkdownForm.form, request.identity, exampleTemplate, MarkdownToHtmlParser.parse(exampleTemplate))))
  }

  def parseMarkdown = UserAwareAction.async { implicit request =>
    MarkdownForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.markdown(form, None, "", ""))),
      data => {
        val textToParse = data.text
        val resultHtml = try {
          MarkdownToHtmlParser.parse(textToParse)
        }
        catch {
          case ex: MarkdownToHtmlParserException => Messages("input.not.parsable") + ex.message.mkString(" [", "", "] ")
          case _: Throwable => Messages("unexpected.exception")
        }
        Future.successful(Ok(views.html.markdown(MarkdownForm.form, request.identity, textToParse, resultHtml)))
      }
    )
  }

  def saveParseOperation(email: String, textToParse: String, resultHtml: String) = UserAwareAction.async { implicit request =>
    val parseOperationInfo = ParseOperationInfo(UUID.randomUUID().toString, email, textToParse, resultHtml, new DateTime())
    collection.insert(parseOperationInfo)
      .map{_ => Redirect(routes.MarkdownController.parseHistory)}
  }

  /**
   * Handles the Parse History action.
   *
   * @return The result to display.
   */
  def parseHistory = SecuredAction.async { implicit request =>
    val user = request.identity
    val query: JsObject = Json.obj("email" -> user.email)
    // the cursor of documents
    val found = collection.find(query).
      sort(Json.obj("creationDateTime" -> -1)).
      cursor[ParseOperationInfo](ReadPreference.primaryPreferred)
    // build (asynchronously) a list containing all the articles
    found.collect[List]().map { parseOperations =>
      Ok(views.html.history(user, parseOperations))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage)
    }
  }

  def deleteParseOperation(id: String) = SecuredAction.async { implicit request =>
    collection.remove(Json.obj("id" -> id))
      .map{_ => Redirect(routes.MarkdownController.parseHistory)}
  }
}
