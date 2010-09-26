/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.psi.tree.TokenSet;

import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Mar 20, 2010
 * Time: 7:30:59 PM
 */
public class LuaLexer extends MergingLexer implements LuaTokenTypes {
//
//
////    private static final TokenSet tokensToMerge = TokenSet.create(
////            SHORTCOMMENT,
////            LONGCOMMENT,
//////            LONGCOMMENT_BEGIN,
//////            LONGCOMMENT_END,
////           STRING,
////           LONGSTRING
////    );
//
    public LuaLexer() {
//     //   super(new MergingLexerAdapter(new LuaFlexLexer(), tokensToMerge));
//    }
//
////    @Override
////    protected void lookAhead(Lexer baseLexer) {
////        final IElementType type = baseLexer.getTokenType();
////
////        super.lookAhead(baseLexer);
////    }
//}

    super(new FlexAdapter(new _LuaLexer((Reader) null)),
                MergeTuple.create(TokenSet.create(LONGCOMMENT_BEGIN, LONGCOMMENT, LONGCOMMENT_END), LONGCOMMENT),
                MergeTuple.create(TokenSet.create(LONGSTRING, LONGSTRING_BEGIN, LONGSTRING_END), LONGSTRING),
                MergeTuple.create(TokenSet.create(STRING), STRING),
                MergeTuple.create(TokenSet.create(SHORTCOMMENT), SHORTCOMMENT));
}}