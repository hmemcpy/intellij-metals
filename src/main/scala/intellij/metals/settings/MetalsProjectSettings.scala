package intellij.metals.settings

import com.intellij.openapi.components._
import com.intellij.openapi.project.Project

import scala.beans.BeanProperty

@State(name = "MetalsProjectSettings", storages = Array(new Storage(StoragePathMacros.WORKSPACE_FILE)))
final class MetalsProjectSettings extends PersistentStateComponent[MetalsProjectSettings.State] {
  @BeanProperty var metalsEnabled: Boolean = true

  override def getState: MetalsProjectSettings.State = {
    val state = new MetalsProjectSettings.State

    state.metalsEnabled = metalsEnabled
    state
  }

  override def loadState(state: MetalsProjectSettings.State): Unit =
    metalsEnabled = state.metalsEnabled
}
object MetalsProjectSettings {
  def getInstance(project: Project): MetalsProjectSettings = project.getService(classOf[MetalsProjectSettings])

  class State {
    @BeanProperty var metalsEnabled: Boolean = true
  }
}
