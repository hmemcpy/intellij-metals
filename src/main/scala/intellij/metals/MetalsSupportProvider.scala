package intellij.metals

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.{LspServerSupportProvider, ProjectWideLspServerDescriptor}
import intellij.metals.settings.{MetalsProjectSettings, MetalsSettings}

final class MetalsSupportProvider extends LspServerSupportProvider {
  override def fileOpened(
    project: Project,
    file: VirtualFile,
    serverStarter: LspServerSupportProvider.LspServerStarter
  ): Unit = {
    val settings        = MetalsSettings.getInstance
    val projectSettings = MetalsProjectSettings.getInstance(project)

    if (settings.metalsPath.nonEmpty && projectSettings.metalsEnabled) {
      serverStarter.ensureServerStarted(MetalsLspServerDescriptor(project, settings.metalsPath))
    }
  }

  private case class MetalsLspServerDescriptor(project: Project, metalsPath: String)
      extends ProjectWideLspServerDescriptor(project, "Metals") {

    override def createCommandLine(): GeneralCommandLine =
      new GeneralCommandLine(metalsPath) // TODO make sure the path actually exists

    override def isSupportedFile(file: VirtualFile): Boolean =
      file.getExtension == "scala"

    // Disable the built-in hover support to disable the hover info tooltip
    // Macro type evaluator will create an ad-hoc hover request to the LSP server
    override def getLspHoverSupport: Boolean = false
  }
}
