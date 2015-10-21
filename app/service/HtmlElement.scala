package service

trait HtmlElement {
  val newline = "\n"

  def openBracket = "<"
  def openCloseBracket = "</"
  def closeBracket = ">"

  def tagName: String
  def content: String

  val openTag = openBracket + tagName + closeBracket
  val closeTag = openCloseBracket + tagName + closeBracket
  override def toString = openTag + content + closeTag
}

trait HtmlElContainer extends HtmlElement {
  override def openBracket = newline + super.openBracket
  override def openCloseBracket = newline + super.openCloseBracket
  def contentElements = List[HtmlElement]()
  def content = contentElements.mkString(" ")
}

case class HtmlElHtml(override val contentElements: List[HtmlElement]) extends HtmlElContainer {
  override def openBracket = "<"
  def tagName = "html"
}

case class HtmlElBody(override val contentElements: List[HtmlElement]) extends HtmlElContainer {
  def tagName = "body"
}

case class HtmlElParagraph(override val contentElements: List[HtmlElement]) extends HtmlElContainer {
  override def openCloseBracket = "</"
  def tagName = "p"
}

case class HtmlElHeader(size: Int, override val content: String) extends HtmlElement {
  def tagName = "h" + size
  override def openBracket = newline + super.openBracket
}

case class HtmlElEmphasized(override val content: String) extends HtmlElement {
  def tagName = "em"
}

case class HtmlElStrong(override val content: String) extends HtmlElement {
  def tagName = "strong"
}

case class HtmlElUrl(override val content: String, link: String) extends HtmlElement {
  def tagName = "a"
  override val openTag = s"""$openBracket$tagName href="$link"$closeBracket"""
}

case class HtmlElPlainText(override val content: String) extends HtmlElement {
  def tagName = ""
  override def toString = content
}