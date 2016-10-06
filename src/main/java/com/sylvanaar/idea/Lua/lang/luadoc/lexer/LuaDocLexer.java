/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.lang.luadoc.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;


public class LuaDocLexer extends MergingLexerAdapter implements LuaDocTokenTypes {

  private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(
      LDOC_COMMENT_DATA,
      LDOC_WHITESPACE
  );

  public LuaDocLexer() {
    super(new FlexAdapter(new _LuaDocLexer()),
        TOKENS_TO_MERGE);
  }
}
