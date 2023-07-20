package metals

import com.intellij.lang.annotation.{Annotation, AnnotationHolder}
import com.intellij.openapi.editor.Editor
import org.eclipse.lsp4j.Diagnostic
import org.wso2.lsp4intellij.contributors.annotator.LSPAnnotator

class MyAnnotator extends LSPAnnotator {

  override def createAnnotation(editor: Editor, holder: AnnotationHolder, diagnostic: Diagnostic): Annotation = {

    val ann = super.createAnnotation(editor, holder, diagnostic)
    println(ann)
    ann

  }
}
