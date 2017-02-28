import javax.servlet.ServletContext
import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.SystemSettingsService.SystemSettings
import io.github.gitbucket.solidbase.model.Version
import me.huzi.gitbucket.desktopnotify.controller.DesktopNotifyController

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "desktopnotify"

  override val pluginName: String = "Desktop Notify Plugin"

  override val description: String = "It will notify the activity to the desktop."

  override val versions: Seq[Version] = Seq(
    new Version("1.0"),
    new Version("3.12"),
    new Version("4.0.0"),
    new Version("4.3.0"),
    new Version("4.10.0")
  )

  override val controllers = Seq(
    "/*" -> new DesktopNotifyController()
  )

  override val assetsMappings = Seq("/desktopnotify/assets" -> "/desktopnotify/assets")

  override def javaScripts(registry: PluginRegistry, context: ServletContext, settings: SystemSettings): Seq[(String, String)] = {
    // Add Snippet link to the header
    val path = settings.baseUrl.getOrElse(context.getContextPath)
    Seq(
      ".*/(?!signin)[^/]*" -> s"""
        |</script>
        |<script>var path = "$path";</script>
        |<script src="$path/plugin-assets/desktopnotify/assets/js.cookie-2.1.2.min.js"></script>
        |<script src="$path/plugin-assets/desktopnotify/assets/desktop-notify-min.js"></script>
        |<script src="$path/plugin-assets/desktopnotify/assets/main.js"></script>
        |<script>
      """.stripMargin
    )
  }

}
