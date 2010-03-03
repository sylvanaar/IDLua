package lua;

import com.intellij.lexer.FlexAdapter;
import com.intellij.psi.tree.IElementType;

import java.io.Reader;

/**
 * @author max
 */
public class LuaParsingLexer extends FlexAdapter {
    private boolean myOnBreakOrContinue = false;
    private boolean myOnSemanticLineFeed = false;

    private final static int ON_BREAK_OR_CONTINUE = 3;
    private final static int ON_SEMANTIC_LF = 4;

    public LuaParsingLexer() {
        super(new _LuaLexer((Reader) null));
    }

    @Override
    public void advance() {
        if (!myOnSemanticLineFeed) {
            super.advance();
            final IElementType type = getTokenType();

            if (myOnBreakOrContinue && type == JSTokenTypes.WHITE_SPACE) {
                boolean hasLineFeed = false;
                for (int i = super.getTokenStart(); i < super.getTokenEnd(); i++) {
                    if (getBuffer()[i] == '\n') {
                        hasLineFeed = true;
                        break;
                    }
                }

                if (hasLineFeed) {
                    myOnSemanticLineFeed = true;
                }
            }

            myOnBreakOrContinue = (type == JSTokenTypes.BREAK_KEYWORD || type == JSTokenTypes.CONTINUE_KEYWORD);
        } else {
            myOnSemanticLineFeed = false;
            myOnBreakOrContinue = false;
        }
    }

    @Override
    public IElementType getTokenType() {
        return myOnSemanticLineFeed ? JSTokenTypes.SEMANTIC_LINEFEED : super.getTokenType();
    }

    @Override
    public int getTokenStart() {
        return super.getTokenStart();
    }

    @Override
    public int getTokenEnd() {
        return myOnSemanticLineFeed ? super.getTokenStart() : super.getTokenEnd();
    }

    @Override
    public int getState() {
        if (myOnSemanticLineFeed) return ON_SEMANTIC_LF;
        if (myOnBreakOrContinue) return ON_BREAK_OR_CONTINUE;
        return super.getState();
    }
}

