package me.huzi.gitbucket.desktopnotify.service

import gitbucket.core.model.Account
import gitbucket.core.model.Profile._
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.service.ActivityService

trait DNotifyActivityService extends ActivityService {

  private def repositories(loginAccount: Option[Account]) = {
    loginAccount match {
      // for Administrators
      case Some(x) if x.isAdmin => Repositories
      // for Normal Users
      case Some(x) if !x.isAdmin =>
        Repositories filter { t =>
          (t.isPrivate === false.bind) || (t.userName === x.userName) ||
          (t.userName in GroupMembers.filter(_.userName === x.userName.bind).map(_.groupName)) ||
          (Collaborators.filter { t2 =>
            t2.byRepository(t.userName, t.repositoryName) &&
              ((t2.collaboratorName === x.userName.bind) || (t2.collaboratorName in GroupMembers.filter(_.userName === x.userName.bind).map(_.groupName)))
          } exists)
        }
      // for Guests
      case None => Repositories filter(_.isPrivate === false.bind)
    }
  }

  def getActivitiesOverId(loginAccount: Option[Account], activityId: Int)(implicit s: Session) =
    Activities
      .join(repositories(loginAccount))
      .on((t1, t2) => t1.byRepository(t2.userName, t2.repositoryName))
      .filter { case (t1, _) => (t1.activityId > activityId.bind) && loginAccount.map(t1.activityUserName =!= _.userName.bind).getOrElse(true.bind) }
      .sortBy { case (t1, _) => t1.activityId desc }
      .map { case (t1, _) => t1 }
      .take(30)
      .list

}
