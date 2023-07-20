package metals;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;

import java.util.concurrent.CompletableFuture;

public class ScalaInteropWithKotlinSux {
    public static CompletableFuture<Hover> requestHover(LanguageServerWrapper lsp, HoverParams params) {
        return lsp.getRequestManager().hover(params);
    }
}
