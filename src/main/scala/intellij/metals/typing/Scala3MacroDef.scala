package intellij.metals.typing

import com.intellij.psi.PsiNamedElement
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction

object Scala3MacroDef {
  def unapply(named: PsiNamedElement): Option[ScFunction] =
    named match {
      case f: ScFunction if f.hasModifierPropertyScala("transparent") => Some(f)
      case _                                                          => None
    }
}
