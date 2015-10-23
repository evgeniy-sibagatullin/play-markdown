package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import forms.MarkdownForm
import models.User
import models.services.UserService
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import service.MarkdownToHtmlParser

import scala.concurrent.Future
import scala.language.postfixOps

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
  clock: Clock)
  extends Silhouette[User, CookieAuthenticator] {

  def parseMarkdown = UserAwareAction.async { implicit request =>
    MarkdownForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.markdown(form, None, "", ""))),
      data => {
        val textToParse = data.text
        val resultHtml = MarkdownToHtmlParser.parse(textToParse)
        Future.successful(Ok(views.html.markdown(MarkdownForm.form, request.identity, textToParse, resultHtml)))
      }
    )
  }
}
