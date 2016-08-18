package me.huzi.gitbucket.desktopnotify.controller

import gitbucket.core.view.helpers
import jp.sf.amateras.scalatra.forms._
import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.AccountService
import gitbucket.core.service.RepositoryService.RepositoryInfo
import gitbucket.core.util._
import gitbucket.core.util.Directory._
import gitbucket.core.util.ControlUtil._
import gitbucket.core.util.Implicits._
import gitbucket.core.view.helpers._
import gitbucket.core.api.JsonFormat
import me.huzi.gitbucket.desktopnotify.service._

class DesktopNotifyController extends DesktopNotifyControllerBase with DNotifyActivityService with UsersAuthenticator

trait DesktopNotifyControllerBase extends ControllerBase {
  self: DNotifyActivityService with UsersAuthenticator =>

  get("/dnotify/id/:id"){
    contentType = "application/json"
    JsonFormat(getActivitiesOverId(context.loginAccount, params("id").toInt))
  }

}
