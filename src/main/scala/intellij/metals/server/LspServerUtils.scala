package intellij.metals.server

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.impl.{LspServerImpl, LspServerManagerImpl}
import intellij.metals.MetalsSupportProvider

import scala.jdk.CollectionConverters.IteratorHasAsScala

object LspServerUtils {
  def forProject(project: Project): Option[LspServer] = {
    val lspManager = LspServerManagerImpl.getInstanceImpl(project)
    lspManager
      .getServersForProvider(classOf[MetalsSupportProvider])
      .iterator()
      .asScala
      .collectFirst {
        case lsp: LspServerImpl if lsp.isRunning => lsp
      }
  }

}
