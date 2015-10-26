package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import forms.MarkdownForm
import models.ParseOperationInfo._
import models.{ParseOperationInfo, User}
import models.services.UserService
import org.joda.time.DateTime
import play.api.Configuration
import play.api.i18n._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo._
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

  // get the collection 'articles'
  val collection = db[JSONCollection]("parseOperations")

  def saveParseOperation(email: String, textToParse: String, resultHtml: String) = UserAwareAction.async { implicit request =>
    val parseOperationInfo = ParseOperationInfo(email, textToParse, resultHtml, new DateTime())
    collection.insert(parseOperationInfo)
    Future.successful(Ok(views.html.markdown(MarkdownForm.form, request.identity, textToParse, resultHtml)))
  }

  /**
   * Handles the Parse History action.
   *
   * @return The result to display.
   */
  def parseHistory = SecuredAction.async { implicit request =>
    val user = request.identity
    val query:JsObject = Json.obj("email" -> user.email)
    // the cursor of documents
    val found = collection.find(query).cursor[ParseOperationInfo](ReadPreference.primaryPreferred)
    // build (asynchronously) a list containing all the articles
    found.collect[List]().map { parseOperations =>
      Ok(views.html.history(user, parseOperations))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage)
    }
  }
}
