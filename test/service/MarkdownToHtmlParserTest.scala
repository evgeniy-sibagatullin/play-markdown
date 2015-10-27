package service

import org.scalatest.FunSuite
import service.MarkdownToHtmlParser.MarkdownToHtmlParserException

class MarkdownToHtmlParserTest extends FunSuite {

  val newline = "\n"
  val markdownParser = MarkdownToHtmlParser
  def wrapHtml(content: String) = { content.mkString(s"<html>$newline<body>$newline", "", s"$newline</body>$newline</html>") }

  /**
   * Positive
   */
  test("plain text") {
    assert(markdownParser.parse("plain text") === wrapHtml(s"<p>plain text</p>"))
  }

  test("header text") {
    assert(markdownParser.parse("## header text") === wrapHtml(s"<h2>header text</h2>"))
  }

  test("emphasized text") {
    assert(markdownParser.parse("*emphasized text*") === wrapHtml(s"<p><em>emphasized text</em></p>"))
  }

  test("strong text") {
    assert(markdownParser.parse("**strong text**") === wrapHtml(s"<p><strong>strong text</strong></p>"))
  }

  test("url") {
    assert(markdownParser.parse("[some text](https://link.com)") === wrapHtml(
      s"""<p><a href=\"https://link.com\">some text</a></p>"""))
  }

  test("few elements in one line") {
    assert(markdownParser.parse("plain and **strong text** with [any link](http://google.wat) and *so* on") === wrapHtml(
      s"""<p>plain and <strong>strong text</strong> with <a href="http://google.wat">any link</a> and <em>so</em> on</p>"""))
  }

  test("multiline test with empty lines") {
    val example = """
        |# Lorem ipsum
        |
        |Dolor sit amet,
        |
        |consetetur *sadipscing* elitr,
        |
        |sed [diam](http://mysite.com) nonumy eirmod tempor
      """.stripMargin
    assert(markdownParser.parse(example) === wrapHtml(
      s"""<h1>Lorem ipsum</h1> \n<p>Dolor sit amet,</p> \n<p>consetetur <em>sadipscing</em> elitr,</p> \n<p>sed <a href="http://mysite.com">diam</a> nonumy eirmod tempor</p>"""))
  }

  /**
   * Negative
   */
  test("empty") {
    assert(markdownParser.parse("") === "Nothing to parse" )
  }

  test("bad header") {
    val brokenText = "#### uh oh"
    assert(intercept[MarkdownToHtmlParserException] (markdownParser.parse(brokenText)).message === brokenText )
  }

  test("redundant star") {
    val brokenText = "some *emphasized text* and * a mistake"
    assert(intercept[MarkdownToHtmlParserException] (markdownParser.parse(brokenText)).message === brokenText )
  }
}
