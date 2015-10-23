package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the markdown text.
 */
object MarkdownForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "text" -> text
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param text The markdown text.
   */
  case class Data(text: String)

}
