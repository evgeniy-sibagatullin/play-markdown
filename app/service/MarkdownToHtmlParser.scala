package service

import scala.util.parsing.combinator.RegexParsers

object MarkdownToHtmlParser extends RegexParsers {
  val newline = "\n"
  val plainTextPat = "[^#*\\[\\]]+".r
  val headerPat = "#{1,3}".r
  val italicPat = "\\*".r
  val strongPat = "\\*{2}".r
  val urlPat = "(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?".r

  def parse(inputText: String) = {
    val inputDataArray = inputText.trim.split(newline).filter(x => x.nonEmpty && x != "\r")

    if (inputDataArray.isEmpty) "Nothing to parse"
    else HtmlElHtml(List(HtmlElBody(inputDataArray.map(parseLine).toList))).toString
  }

  def urlTag: Parser[HtmlElement] = "[" ~ plainText ~ "]" ~ "(" ~ urlPat ~ ")" ^^ {
    case "[" ~ text ~ "]" ~ "(" ~ url ~ ")" => HtmlElUrl(text.content, url)
  }

  private def parseLine(inputLine: String) = {
    parseAll(lineTag, inputLine).getOrElse(throw MarkdownToHtmlParserException(inputLine))
  }

  private def lineTag: Parser[HtmlElement] = headerLineTag | simpleLineTag

  private def headerLineTag: Parser[HtmlElement] = (headerPat ~ plainText) ^^ {
    case h ~ text => HtmlElHeader(h.length, text.content)
  }

  private def simpleLineTag: Parser[HtmlElContainer] = (emphasizedTag | strongTag | urlTag | plainText) ~ rep(simpleLineTag) ^^ {
    case text ~ Nil => HtmlElParagraph(text :: Nil)
    case text ~ list => HtmlElParagraph(text :: list.head.contentElements)
  }

  private def emphasizedTag: Parser[HtmlElement] = italicPat ~> plainText <~ italicPat ^^ {
    case text => HtmlElEmphasized(text.content)
  }

  private def strongTag: Parser[HtmlElement] = strongPat ~> plainText <~ strongPat ^^ {
    case text => HtmlElStrong(text.content)
  }

  private def plainText: Parser[HtmlElement] = plainTextPat ^^ { case t => HtmlElPlainText(t.trim.replaceAll("\\s+", " ")) }

  case class MarkdownToHtmlParserException(message: String) extends RuntimeException(message)

}
