package me.huzi.gitbucket.desktopnotify.service

import gitbucket.core.model.{Account, Activity}
import gitbucket.core.model.Profile._
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.service.{ActivityService, RepositoryService}
import gitbucket.core.util.Directory.ActivityLog
import scala.collection.mutable

trait DNotifyActivityService {
  self: RepositoryService =>

  /**
   * Activities of other users newer than `since` in repositories the given user can read, newest first, and the id
   * of the newest activity (the next cursor).
   */
  def getActivitiesSince(loginAccount: Account, since: Option[String])(implicit
    s: Session
  ): (Option[String], List[Activity]) = {
    if (!ActivityService.isNewsFeedEnabled) {
      (None, Nil)
    } else {
      val readable = mutable.Map[(String, String), Boolean]()
      ActivityLogReader.readSince(ActivityLog, since) { activity =>
        activity.activityUserName != loginAccount.userName &&
        readable.getOrElseUpdate(
          (activity.userName, activity.repositoryName),
          Repositories
            .filter(_.byRepository(activity.userName, activity.repositoryName))
            .firstOption
            .exists(isReadable(_, Some(loginAccount)))
        )
      }
    }
  }
}
