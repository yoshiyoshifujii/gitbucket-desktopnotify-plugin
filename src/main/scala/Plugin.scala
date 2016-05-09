import javax.servlet.ServletContext
import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.SystemSettingsService.SystemSettings
import io.github.gitbucket.solidbase.model.Version
import gitbucket.core.controller.ControllerBase
import me.huzi.gitbucket.desktopnotify.controller.DesktopNotifyController

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "desktopnotify"

  override val pluginName: String = "Desktop Notify Plugin"

  override val description: String = "It will notify the activity to the desktop."

  override val versions: List[Version] = List(
    new Version("4.0.0"),
    new Version("3.12"),
    new Version("1.0")
  )

  override val controllers = Seq(
    "/*" -> new DesktopNotifyController()
  )

  override def javaScripts(registry: PluginRegistry, context: ServletContext, settings: SystemSettings): Seq[(String, String)] = {
    // Add Snippet link to the header
    val path = settings.baseUrl.getOrElse(context.getContextPath)
    Seq(
      ".*" -> s"""
        |</script>
        |<script src="%s/dnotify/js"></script>
        |<script>
      """.format(
        path
      ).stripMargin
    )
  }

}
