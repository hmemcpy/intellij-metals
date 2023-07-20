package intellij.metals.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components._
import com.intellij.openapi.project.Project

import scala.beans.BeanProperty

@State(name = "MetalsSettings", storages = Array(new Storage(value = "metals.xml")))
final class MetalsSettings extends PersistentStateComponent[MetalsSettings.State] {
  @BeanProperty var metalsPath: String = ""

  override def getState: MetalsSettings.State = {
    val state = new MetalsSettings.State

    state.metalsPath = metalsPath
    state
  }

  override def loadState(state: MetalsSettings.State): Unit =
    metalsPath = state.metalsPath
}
object MetalsSettings {
  def getInstance: MetalsSettings =
    ApplicationManager.getApplication.getService(classOf[MetalsSettings])

  class State {
    @BeanProperty var metalsPath: String = ""
  }
}
