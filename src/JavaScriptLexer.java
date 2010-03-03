package lua;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;


public class JavaScriptLexer extends FlexAdapter {
    public JavaScriptLexer() {
        super(new _JavaScriptLexer((Reader) null));
    }
}

