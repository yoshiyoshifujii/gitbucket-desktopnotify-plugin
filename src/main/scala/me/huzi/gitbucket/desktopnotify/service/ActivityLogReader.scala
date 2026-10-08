package me.huzi.gitbucket.desktopnotify.service

import java.io.File
import java.nio.charset.StandardCharsets
import gitbucket.core.model.Activity
import org.apache.commons.io.input.ReversedLinesFileReader
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import scala.collection.mutable.ListBuffer
import scala.util.{Try, Using}

/**
 * Reads GITBUCKET_HOME/activity.log (one JSON activity per line, oldest first), which replaced the ACTIVITY table in
 * GitBucket 4.34.0.
 */
object ActivityLogReader {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  /**
   * Returns the id of the newest activity and the accepted activities newer than `since`, newest first.
   *
   * Without `since` (first poll) only the newest id is returned. If `since` isn't found within the last `maxLines`
   * lines (cursor too old, or from another instance), no activities are returned either, so a stale cursor is reset
   * instead of flooding the user.
   */
  def readSince(file: File, since: Option[String], maxLines: Int = 1000, maxResults: Int = 10)(
    accept: Activity => Boolean
  ): (Option[String], List[Activity]) = {
    if (!file.exists()) {
      (None, Nil)
    } else {
      Using.resource(
        ReversedLinesFileReader.builder().setFile(file).setCharset(StandardCharsets.UTF_8).get()
      ) { reader =>
        var newest: Option[String] = None
        var found = false
        var lines = 0
        val result = new ListBuffer[Activity]
        var line = reader.readLine()
        while (line != null && !found && lines < maxLines) {
          lines += 1
          Try(read[Activity](line)).toOption.foreach { activity =>
            if (newest.isEmpty) newest = Some(activity.activityId)
            if (since.isEmpty || since.contains(activity.activityId)) {
              found = true
            } else if (result.length < maxResults && accept(activity)) {
              result += activity
            }
          }
          line = if (found) null else reader.readLine()
        }
        (newest, if (found) result.toList else Nil)
      }
    }
  }

  private val Markup = Seq(
    """\[(?:issue|pullreq):([^\s]+?/[^\s]+?#\d+)\]""".r -> "$1",
    """\[repo:([^\s]+?/[^\s]+?)\]""".r -> "$1",
    """\[(?:branch|tag):[^\s]+?/[^\s]+?#([^\s]+?)\]""".r -> "$1",
    """\[user:([^\s]+?)\]""".r -> "$1",
    """\[commit:([^\s]+?/[^\s]+?)@([0-9a-f]{7})[0-9a-f]*\]""".r -> "$1@$2",
    """\[release:[^\s]+?/[^\s]+?/[^\s]+?:(.+)\]""".r -> "$1"
  )

  /** Turns the markup of an activity message (`[user:root] opened issue [issue:root/repo#1]`) into plain text. */
  def plainText(message: String): String =
    Markup.foldLeft(message) { case (text, (regex, replacement)) => regex.replaceAllIn(text, replacement) }

  /** Path of the page an activity is about, relative to the GitBucket base URL. */
  def pagePath(activity: Activity): String = {
    val issue = """\[(issue|pullreq):([^\s]+?)/([^\s]+?)#(\d+)\]""".r
    issue.findFirstMatchIn(activity.message) match {
      case Some(m) =>
        val kind = if (m.group(1) == "issue") "issues" else "pull"
        s"/${m.group(2)}/${m.group(3)}/$kind/${m.group(4)}"
      case None => s"/${activity.userName}/${activity.repositoryName}"
    }
  }
}
