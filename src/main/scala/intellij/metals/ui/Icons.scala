package intellij.metals.ui

import com.intellij.openapi.util.IconLoader

import javax.swing.Icon

object Icons {
  val Metals: Icon         = IconLoader.getIcon("/icons/metals.svg", classOf[Icons.type])
  val MetalsDisabled: Icon = IconLoader.getDisabledIcon(Metals)
}
