package me.huzi.gitbucket.desktopnotify.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.util.Implicits._
import me.huzi.gitbucket.desktopnotify.service._
import org.json4s.jackson.Serialization
import org.scalatra.Unauthorized

class DesktopNotifyController extends DesktopNotifyControllerBase with DNotifyActivityService with RepositoryService
    with AccountService

trait DesktopNotifyControllerBase extends ControllerBase {
  self: DNotifyActivityService =>

  // Polled by main.js. Without `since` it only returns the cursor to start from.
  get("/dnotify/activities") {
    context.loginAccount match {
      case None => Unauthorized()
      case Some(account) =>
        contentType = formats("json")
        response.setHeader("Cache-Control", "no-store")
        val (latest, activities) = getActivitiesSince(account, params.get("since").filter(_.nonEmpty))
        Serialization.write(
          Map(
            "latest" -> latest,
            "activities" -> activities.map { activity =>
              Map(
                "id" -> activity.activityId,
                "type" -> activity.activityType,
                "user" -> activity.activityUserName,
                "repository" -> s"${activity.userName}/${activity.repositoryName}",
                "message" -> ActivityLogReader.plainText(activity.message),
                "additionalInfo" -> activity.additionalInfo.map(_.take(200)),
                "url" -> (context.baseUrl + ActivityLogReader.pagePath(activity))
              )
            }
          )
        )
    }
  }
}
