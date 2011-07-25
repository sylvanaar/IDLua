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

package com.sylvanaar.idea.Lua.lang.luadoc.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.editor.highlighter.LuaHighlightingData;
import com.sylvanaar.idea.Lua.lang.luadoc.lexer.LuaDocLexer;
import com.sylvanaar.idea.Lua.lang.luadoc.lexer.LuaDocTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ilyas
 */
public class LuaDocSyntaxHighlighter extends SyntaxHighlighterBase implements LuaDocTokenTypes {

  private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType, TextAttributesKey>();
  
  @NotNull
  public Lexer getHighlightingLexer() {
    return new LuaDocHighlightingLexer();
  }

  static final TokenSet lDOC_COMMENT_TAGS = TokenSet.create(
      LDOC_TAG_NAME
  );

  static final TokenSet lDOC_COMMENT_CONTENT = TokenSet.create(
      LDOC_COMMENT_DATA, LDOC_DASHES, LDOC_COMMENT_START
  );

  static final TokenSet lDOC_COMMENT_VALUES = TokenSet.create(
      LDOC_TAG_VALUE
  );

  static {
    fillMap(ATTRIBUTES, lDOC_COMMENT_CONTENT, LuaHighlightingData.LUADOC);
    fillMap(ATTRIBUTES, lDOC_COMMENT_TAGS, LuaHighlightingData.LUADOC_TAG);
    fillMap(ATTRIBUTES, lDOC_COMMENT_VALUES, LuaHighlightingData.LUADOC_VALUE);
  }


  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType type) {
    return pack(ATTRIBUTES.get(type));
  }

  private static class LuaDocHighlightingLexer extends LuaDocLexer {
    public IElementType getTokenType() {
      return super.getTokenType() == LDOC_TAG_NAME ? LDOC_TAG_NAME : super.getTokenType();
    }
  }
}
