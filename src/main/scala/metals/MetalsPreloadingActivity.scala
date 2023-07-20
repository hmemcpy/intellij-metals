package metals

import com.intellij.openapi.application.PreloadingActivity
import org.wso2.lsp4intellij.IntellijLanguageClient
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.RawCommandServerDefinition

class MetalsPreloadingActivity extends PreloadingActivity {
  override def preload(): Unit = {
    val command = Array( /*"/Users/hmemcpy/git/langoustine-tracer", */ "/Users/hmemcpy/git/metals")

    IntellijLanguageClient.addServerDefinition(new RawCommandServerDefinition("scala", command))
  }
}
