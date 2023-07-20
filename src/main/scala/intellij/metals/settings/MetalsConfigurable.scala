package intellij.metals.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextComponentAccessors
import com.intellij.openapi.util.Disposer
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton

import java.awt.BorderLayout
import javax.swing.{JComponent, JLabel, JPanel}

class MetalsConfigurable(project: Project) extends SearchableConfigurable with Disposable {
  private var panel: JPanel                                   = _
  private var pathField: TextFieldWithHistoryWithBrowseButton = _

  private val metalsSettings = MetalsSettings.getInstance

  override def getId: String = "metals.settings.configurable"

  override def getDisplayName: String = "Metals"

  override def createComponent(): JComponent = {
    panel = new JPanel(new BorderLayout(10, 5))
    val contentPanel = new JPanel(new BorderLayout(4, 0))
    panel.add(contentPanel, BorderLayout.NORTH)

    contentPanel.add(new JLabel("Metals executable path:"), BorderLayout.WEST)
    pathField = new TextFieldWithHistoryWithBrowseButton
    contentPanel.add(pathField)
    pathField.addBrowseFolderListener(
      "Select Metals executable",
      "",
      project,
      FileChooserDescriptorFactory.createSingleFileDescriptor(),
      TextComponentAccessors.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT
    )

    pathField.setText(metalsSettings.metalsPath)

    panel
  }

  override def isModified: Boolean =
    pathField.getText.trim != metalsSettings.metalsPath

  override def apply(): Unit =
    metalsSettings.metalsPath = pathField.getText.trim

  override def disposeUIResources(): Unit = Disposer.dispose(this)

  override def dispose(): Unit = panel = null
}
