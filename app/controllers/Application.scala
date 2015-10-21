package controllers

import play.api.mvc._
import service.MarkdownToHtmlParser

class Application extends Controller {

  val exampleTemplate = "" +
    "# huge header\n" +
    "### modest header\n" +
    "plain text\n" +
    "*a little emphasized text*\n" +
    "**very strong text**\n" +
    "[link](http://mysite.com)"

  def markdown = Action {
    Ok(views.html.markdown(exampleTemplate, MarkdownToHtmlParser.parse(exampleTemplate)))
  }

  def markdownParse = Action { implicit request =>
    val textToParse = request.body.asFormUrlEncoded.get("markdown-form").head.trim
    val resultHtml = MarkdownToHtmlParser.parse(textToParse)
    Ok(views.html.markdown(textToParse, resultHtml))
  }
}
