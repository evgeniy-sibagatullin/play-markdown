package models

import org.joda.time.DateTime

/**
 * The user object.
 * @param id The unique ID of the parse operation.
 * @param email The client email.
 * @param textToParse Markdown input text.
 * @param resultHtml Successfully generated HTML.
 * @param creationDateTime Surprisingly creation date and time.
 */
case class ParseOperation (
  id: String,
  email: String,
  textToParse: String,
  resultHtml: String,
  creationDateTime: DateTime
)

/**
 * The companion object.
 */
object ParseOperation {
  import play.api.libs.json._

  /**
   * Converts the [ParseOperationInfo] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[ParseOperation]

}