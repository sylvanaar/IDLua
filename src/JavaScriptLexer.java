package lua;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;


public class LuaScriptLexer extends FlexAdapter {
    public LuaScriptLexer() {
        super(new _LuaScriptLexer((Reader) *null));
    }
}

