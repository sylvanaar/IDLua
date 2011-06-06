/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

/* Decompiled through IntelliJad */


package com.intellij.lexer;

import com.intellij.psi.tree.IElementType;

// Referenced classes of package com.intellij.lexer:
//            DelegateLexer, Lexer, LexerPosition

public class MergingLexerAdapterBase extends DelegateLexer
{
    protected static interface MergeFunction
    {

        public abstract IElementType merge(IElementType ielementtype, Lexer lexer);
    }

    private static class MyLexerPosition
        implements LexerPosition
    {

        private final int myOffset, myOldState;
        private final IElementType myTokenType;
        private final LexerPosition myOriginalPosition;

        public int getOffset()
        {
            return myOffset;
        }

        public int getState()
        {
            return myOriginalPosition.getState();
        }

        public IElementType getType()
        {
            return myTokenType;
        }

        public LexerPosition getOriginalPosition()
        {
            return myOriginalPosition;
        }

        public int getOldState()
        {
            return myOldState;
        }

        public MyLexerPosition(int offset, IElementType tokenType, LexerPosition originalPosition, int oldState)
        {
            myOffset = offset;
            myTokenType = tokenType;
            myOriginalPosition = originalPosition;
            myOldState = oldState;
        }
    }


    private IElementType myTokenType;
    private int myState, myTokenStart;
    private final MergeFunction myMergeFunction;

    public MergingLexerAdapterBase(Lexer original, MergeFunction mergeFunction)
    {
        super(original);
        myMergeFunction = mergeFunction;
    }

    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState)
    {
        super.start(buffer, startOffset, endOffset, initialState);
        myTokenType = null;
    }

    public int getState()
    {
        locateToken();
        return myState;
    }

    public IElementType getTokenType()
    {
        locateToken();
        return myTokenType;
    }

    public int getTokenStart()
    {
        locateToken();
        return myTokenStart;
    }

    public int getTokenEnd()
    {
        locateToken();
        return super.getTokenStart();
    }

    public void advance()
    {
        myTokenType = null;
    }

    private void locateToken()
    {
        if(myTokenType == null)
        {
            Lexer orig = getDelegate();
            myTokenType = orig.getTokenType();
            myTokenStart = orig.getTokenStart();
            myState = orig.getState();
            if(myTokenType == null)
                return;
            orig.advance();
            myTokenType = myMergeFunction.merge(myTokenType, orig);
        }
    }

    public Lexer getOriginal()
    {
        return getDelegate();
    }

    public void restore(LexerPosition position)
    {
        MyLexerPosition pos = (MyLexerPosition)position;
        getDelegate().restore(pos.getOriginalPosition());
        myTokenType = pos.getType();
        myTokenStart = pos.getOffset();
        myState = pos.getOldState();
    }

    public LexerPosition getCurrentPosition()
    {
        return new MyLexerPosition(myTokenStart, myTokenType, getDelegate().getCurrentPosition(), myState);
    }
}
