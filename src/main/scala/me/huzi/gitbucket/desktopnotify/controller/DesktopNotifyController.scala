package me.huzi.gitbucket.desktopnotify.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.util._
import gitbucket.core.util.Implicits._
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
