package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.MarkdownForm
import models.{ParseOperation, User}
import org.joda.time.DateTime
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
 * @param reactiveMongoApi The ReactiveMongo API
 */
class MarkdownController @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[User, CookieAuthenticator],
  val reactiveMongoApi: ReactiveMongoApi)
  extends Silhouette[User, CookieAuthenticator] with Controller with MongoController with ReactiveMongoComponents {

  val collection = db[JSONCollection]("parseOperations")

  /**
   * Handles the "Markdown Home" action.
   *
   * @return The result to display.
   */
  def index = UserAwareAction.async { implicit request =>
    val exampleText = Messages("markdown.example.text")
    Future.successful(Ok(views.html.markdown(MarkdownForm.form, request.identity, exampleText, MarkdownToHtmlParser.parse(exampleText))))
  }

  /**
   * Handles the "Markdown Parse" action.
   *
   * @return The result to display.
   */
  def parseMarkdown = UserAwareAction.async { implicit request =>
    MarkdownForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.markdown(form, None, "", ""))),
      data => {
        val textToParse = data.text
        val resultHtml = try {
          MarkdownToHtmlParser.parse(textToParse)
        }
        catch {
          case ex: MarkdownToHtmlParserException => Messages("error.input.not.parsable") + ex.message.mkString(" [", "", "] ")
          case _: Throwable => Messages("error.unexpected.exception")
        }
        Future.successful(Ok(views.html.markdown(MarkdownForm.form, request.identity, textToParse, resultHtml)))
      }
    )
  }

  /**
   * Handles the "Markdown Save" action.
   *
   * @return The result to display.
   */
  def saveParseOperation(email: String, textToParse: String, resultHtml: String) = UserAwareAction.async { implicit request =>
    val parseOperationInfo = ParseOperation(UUID.randomUUID().toString, email, textToParse, resultHtml, new DateTime())
    collection.insert(parseOperationInfo).map { _ => Redirect(routes.MarkdownController.parseHistory) }.
      recover { case e => logger.error(Messages("error.db"), e); BadRequest(e.getMessage) }
  }

  /**
   * Handles the "Parse History" action.
   *
   * @return The result to display.
   */
  def parseHistory = SecuredAction.async { implicit request =>
    val user = request.identity
    val query: JsObject = Json.obj("email" -> user.email)
    val found = collection.find(query).
      sort(Json.obj("creationDateTime" -> -1)).
      cursor[ParseOperation](ReadPreference.primaryPreferred)
    found.collect[List]().map { parseOperations => Ok(views.html.history(user, parseOperations)) }.
      recover { case e => logger.error(Messages("error.db"), e); BadRequest(e.getMessage) }
  }

  /**
   * Handles the "Parse History Delete" action.
   *
   * @return The result to display.
   */
  def deleteParseOperation(id: String) = SecuredAction.async { implicit request =>
    collection.remove(Json.obj("id" -> id)).
      map { _ => Redirect(routes.MarkdownController.parseHistory) }.
      recover { case e => logger.error(Messages("error.db"), e); BadRequest(e.getMessage) }
  }
}
