package intellij.metals.ui

import com.intellij.openapi.fileEditor.{FileEditorManager, FileEditorManagerListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.openapi.wm.{StatusBarWidget, StatusBarWidgetFactory, WindowManager}
import com.intellij.util.messages.MessageBusConnection
import intellij.metals.server.LspServerUtils

import java.util.concurrent.atomic.AtomicReference
import javax.swing.Icon

final class MetalsStatusWidgetFactory extends StatusBarWidgetFactory {

  override def getId: String = "Metals LSP"

  override def getDisplayName: String = "Metals (Scala LSP)"

  override def createWidget(project: Project): StatusBarWidget =
    new MetalsStatusWidget(project)

  private class MetalsStatusWidget(project: Project) extends EditorBasedWidget(project) with FileEditorManagerListener {
    private val icon: AtomicReference[Icon] = new AtomicReference[Icon](Icons.MetalsDisabled)

    override def ID(): String = s"Metals LSP: ${project.getName}"

    override def getPresentation: StatusBarWidget.WidgetPresentation =
      new StatusBarWidget.IconPresentation {
        override def getIcon: Icon = icon.get()

        override def getTooltipText: String =
          if (getIcon == Icons.Metals) s"Metals is running: ${project.getName}"
          else "Metals is disconnected"
      }

    override def registerCustomListeners(connection: MessageBusConnection): Unit =
      connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this)

    override def fileOpened(source: FileEditorManager, file: VirtualFile): Unit =
      (for {
        manager   <- Option(WindowManager.getInstance)
        statusBar <- Option(manager.getStatusBar(project))
      } yield statusBar).foreach { statusBar =>
        icon.set(updatedIcon)
        statusBar.updateWidget(ID())
      }

    private def updatedIcon: Icon =
      LspServerUtils
        .forProject(project)
        .map(_ => Icons.Metals)
        .getOrElse(Icons.MetalsDisabled)
  }
}
