package metals

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import metals.ScalaInteropWithKotlinSux.requestHover
import org.eclipse.lsp4j.{HoverParams, TextDocumentIdentifier}
import org.jetbrains.plugins.scala.extensions.PsiElementExt
import org.jetbrains.plugins.scala.lang.macros.MacroDef
import org.jetbrains.plugins.scala.lang.macros.evaluator.{MacroContext, ScalaMacroEvaluator}
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScMethodCall, ScReferenceExpression}
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.worksheet.WorksheetUtils
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper
import org.wso2.lsp4intellij.requests.{HoverHandler, Timeouts}
import org.wso2.lsp4intellij.utils.ApplicationUtils.computableReadAction
import org.wso2.lsp4intellij.utils.{DocumentUtils, FileUtils}

import scala.concurrent.duration.Duration
import scala.concurrent.{duration, Await, TimeoutException}
import scala.jdk.FutureConverters.CompletionStageOps

final class HackMacroEvaluator(project: Project) extends ScalaMacroEvaluator(project) {
  val regex = raw"""(?ms)(?<=<code class="language-scala">)(.*?)(?=</code>)""".r
  val lsp   = LanguageServerWrapper.forProject(project)

  override def checkMacro(element: PsiNamedElement, context: MacroContext): Option[ScType] =
    element match {
      case MacroDef(m) if m.isDefinedInClass =>
        resolveMacroType(m, context) orElse super.checkMacro(element, context)
      case _ => super.checkMacro(element, context)
    }

  private def resolveMacroType(m: ScFunction, context: MacroContext): Option[ScType] = {
    def extractMacroReference =
      context.place.children.collectFirst {
        case mc: ScMethodCall => mc.getEffectiveInvokedExpr
      }.collectFirst {
        case ref: ScReferenceExpression if ref.resolve() == m => ref
      }

    (for {
      ref      <- extractMacroReference
      hover    <- requestHoverFromLsp(ref)
      hoverText = HoverHandler.getHoverString(hover)
      code     <- regex.findFirstMatchIn(hoverText)
      scType   <- ScalaPsiElementFactory.createTypeFromText(code.matched.trim, context.place, null)
    } yield scType)
  }

  private def requestHoverFromLsp(expr: ScExpression) =
    for {
      editor    <- WorksheetUtils.getSelectedTextEditor(project, expr.getContainingFile.getVirtualFile)
      editorPos  = editor.offsetToLogicalPosition(expr.getTextOffset)
      identifier = new TextDocumentIdentifier(FileUtils.editorToURIString(editor))
      serverPos  = computableReadAction(() => DocumentUtils.logicalToLSPPos(editorPos, editor))
      hover <- try {
                 implicit val ec = scala.concurrent.ExecutionContext.global

                 Await.result(
                   requestHover(lsp, new HoverParams(identifier, serverPos)).asScala
                     .map(Some(_))
                     .recover { case _ => None },
                   Duration(Timeouts.HOVER.getDefaultTimeout, duration.MILLISECONDS)
                 )
               } catch {
                 case _: TimeoutException => None
               }
    } yield hover
}
