package intellij.metals.typing

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.impl.requests.LspHoverRequest
import com.intellij.platform.lsp.util.Lsp4jUtilKt
import com.intellij.psi.{PsiElement, PsiNamedElement}
import intellij.metals.MetalsSupportProvider
import org.apache.commons.text.StringEscapeUtils
import org.jetbrains.plugins.scala.extensions.PsiElementExt
import org.jetbrains.plugins.scala.lang.macros.MacroDef
import org.jetbrains.plugins.scala.lang.macros.evaluator.{MacroContext, ScalaMacroEvaluator}
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScMethodCall, ScReferenceExpression}
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.worksheet.WorksheetUtils

import scala.jdk.CollectionConverters.CollectionHasAsScala

final class MetalsTypeSignatureEvaluator(project: Project) extends ScalaMacroEvaluator(project) {
  lazy val lsp = LspServerManager.getInstance(project)
  val regex    = raw"""(?ms)(?<=<code class="language-scala">)(.*?)(?=</code>)""".r

  override def checkMacro(element: PsiNamedElement, context: MacroContext): Option[ScType] =
    element match {
      case MacroDef(m) if m.isDefinedInClass =>
        resolveMacroType(m, context) orElse super.checkMacro(element, context)
      case Scala3MacroDef(m) =>
        resolveMacroType(m, context)
      case _ =>
        super.checkMacro(element, context)
    }

  private def resolveMacroType(m: PsiElement, context: MacroContext): Option[ScType] = {
    def extractMacroReference =
      context.place.children.collectFirst {
        case mc: ScMethodCall => mc.getEffectiveInvokedExpr
      }.collectFirst {
        case ref: ScReferenceExpression if ref.resolve() == m => ref
      }

    for {
      ref      <- extractMacroReference
      hover    <- requestHoverFromLsp(ref)
      hoverText = Lsp4jUtilKt.convertMarkupContentToHtml(hover.getMarkupContent)
      unescaped = StringEscapeUtils.unescapeHtml4(hoverText)
      code     <- regex.findFirstMatchIn(unescaped)
      scType   <- ScalaPsiElementFactory.createTypeFromText(code.matched.trim, context.place, null)
    } yield scType
  }

  private def requestHoverFromLsp(element: PsiElement) =
    for {
      vf        <- element.containingVirtualFile
      editor    <- WorksheetUtils.getSelectedTextEditor(project, vf)
      lspServer <- lsp.getServersForProvider(classOf[MetalsSupportProvider]).asScala.headOption
      hoverResponse <- Option(
                         lspServer.getRequestExecutor.sendRequestSync(
                           new LspHoverRequest(lspServer, vf, editor.getDocument, element.getTextOffset)
                         )
                       )
    } yield hoverResponse

}
