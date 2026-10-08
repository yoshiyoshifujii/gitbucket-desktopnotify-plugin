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
    new Version("4.10.0"),
    new Version("4.32.0"),
    new Version("5.0.0")
  )

  override val controllers = Seq(
    "/*" -> new DesktopNotifyController()
  )

  override val assetsMappings = Seq("/desktopnotify/assets" -> "/desktopnotify/assets")

  override def javaScripts(
    registry: PluginRegistry,
    context: ServletContext,
    settings: SystemSettings
  ): Seq[(String, String)] = {
    // The context path, not base_url: requests then stay on the origin the page was loaded from
    val path = context.getContextPath.replace("\\", "\\\\").replace("\"", "\\\"")
    Seq(
      ".*/(?!signin)[^/]*" -> s"""
        |(function () {
        |  var script = document.createElement("script");
        |  script.src = "$path/plugin-assets/desktopnotify/assets/main.js";
        |  script.dataset.path = "$path";
        |  script.defer = true;
        |  document.head.appendChild(script);
        |})();
      """.stripMargin
    )
  }

}
