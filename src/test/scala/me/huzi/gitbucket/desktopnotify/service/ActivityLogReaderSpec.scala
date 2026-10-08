package me.huzi.gitbucket.desktopnotify.service

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.Date
import gitbucket.core.model.Activity
import org.json4s._
import org.json4s.jackson.Serialization
import org.scalatest.funsuite.AnyFunSuite

class ActivityLogReaderSpec extends AnyFunSuite {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private def activity(id: String, user: String = "alice", repo: String = "repo", message: String = "msg") =
    Activity("owner", repo, user, "open_issue", message, None, new Date(0), id)

  // Oldest first, like GitBucket writes it
  private def log(lines: String*): File = {
    val file = File.createTempFile("activity", ".log")
    file.deleteOnExit()
    Files.write(file.toPath, lines.map(_ + "\n").mkString.getBytes(StandardCharsets.UTF_8))
    file
  }

  private def logOf(activities: Activity*): File = log(activities.map(Serialization.write(_)): _*)

  private def ids(result: (Option[String], List[Activity])) = (result._1, result._2.map(_.activityId))

  test("first poll returns only the newest id") {
    val file = logOf(activity("a"), activity("b"))
    assert(ids(ActivityLogReader.readSince(file, None)(_ => true)) == (Some("b"), Nil))
  }

  test("returns activities newer than the cursor, newest first") {
    val file = logOf(activity("a"), activity("b"), activity("c"), activity("d"))
    assert(ids(ActivityLogReader.readSince(file, Some("b"))(_ => true)) == (Some("d"), List("d", "c")))
  }

  test("nothing new") {
    val file = logOf(activity("a"), activity("b"))
    assert(ids(ActivityLogReader.readSince(file, Some("b"))(_ => true)) == (Some("b"), Nil))
  }

  test("applies the filter but still moves the cursor") {
    val file = logOf(activity("a"), activity("b", user = "me"), activity("c", user = "me"))
    assert(ids(ActivityLogReader.readSince(file, Some("a"))(_.activityUserName != "me")) == (Some("c"), Nil))
  }

  test("an unknown cursor resets without returning activities") {
    val file = logOf(activity("a"), activity("b"))
    assert(ids(ActivityLogReader.readSince(file, Some("gone"))(_ => true)) == (Some("b"), Nil))
  }

  test("a cursor older than maxLines resets too") {
    val file = logOf(activity("a"), activity("b"), activity("c"), activity("d"))
    assert(ids(ActivityLogReader.readSince(file, Some("a"), maxLines = 2)(_ => true)) == (Some("d"), Nil))
  }

  test("returns at most maxResults activities") {
    val file = logOf((1 to 20).map(i => activity(i.toString)): _*)
    val (_, list) = ActivityLogReader.readSince(file, Some("1"), maxResults = 3)(_ => true)
    assert(list.map(_.activityId) == List("20", "19", "18"))
  }

  test("skips lines that aren't activities") {
    val file = log(Serialization.write(activity("a")), "", "not json", Serialization.write(activity("b")), "{broken")
    assert(ids(ActivityLogReader.readSince(file, Some("a"))(_ => true)) == (Some("b"), List("b")))
  }

  test("missing or empty log") {
    assert(ActivityLogReader.readSince(new File("/nonexistent/activity.log"), None)(_ => true) == (None, Nil))
    assert(ActivityLogReader.readSince(log(), Some("a"))(_ => true) == (None, Nil))
  }

  test("plain text of activity messages") {
    assert(
      ActivityLogReader.plainText("[user:alice] opened issue [issue:owner/repo#12]") == "alice opened issue owner/repo#12"
    )
    assert(
      ActivityLogReader.plainText("[user:alice] pushed to [branch:owner/repo#feature/x] at [repo:owner/repo]")
        == "alice pushed to feature/x at owner/repo"
    )
    assert(
      ActivityLogReader.plainText("[user:alice] commented on commit [commit:owner/repo@0123456789abcdef]")
        == "alice commented on commit owner/repo@0123456"
    )
    assert(
      ActivityLogReader.plainText("[user:alice] released [release:owner/repo/v1.0:Version $1 \\o/]")
        == "alice released Version $1 \\o/"
    )
  }

  test("page path of an activity") {
    assert(ActivityLogReader.pagePath(activity("a", message = "[user:a] closed [pullreq:owner/repo#3]")) == "/owner/repo/pull/3")
    assert(ActivityLogReader.pagePath(activity("a", message = "[user:a] opened [issue:owner/repo#4]")) == "/owner/repo/issues/4")
    assert(ActivityLogReader.pagePath(activity("a", message = "[user:a] created [repo:owner/repo]")) == "/owner/repo")
  }
}
