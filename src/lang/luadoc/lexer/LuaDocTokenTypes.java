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

package com.sylvanaar.idea.Lua.lang.luadoc.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.elements.LuaDocTagValueTokenType;

/**
 * @author ilyas
 */
public interface LuaDocTokenTypes {

  IElementType LDOC_TAG_NAME = new LuaDocElementTypeImpl("LDOC_TAG_NAME");
    IElementType LDOC_TAG_PLAIN_VALUE_TOKEN = new LuaDocElementTypeImpl("LDOC_TAG_PLAIN_VALUE_TOKEN");
    IElementType LDOC_TAG_VALUE = new LuaDocTagValueTokenType();

  IElementType LDOC_COMMENT_START = new LuaDocElementTypeImpl("LDOC_COMMENT_START");
  
  IElementType LDOC_COMMENT_DATA = new LuaDocElementTypeImpl("LDOC_COMMENT_DATA");

  IElementType LDOC_WHITESPACE = new LuaDocElementTypeImpl("LDOC_WHITESPACE");
  IElementType LDOC_DASHES = new LuaDocElementTypeImpl("LDOC_DASHES");
  IElementType LDOC_COMMENT_BAD_CHARACTER = TokenType.BAD_CHARACTER;


  TokenSet LUA_DOC_TOKENS = TokenSet.create(
      LDOC_COMMENT_START,

      LDOC_COMMENT_DATA,
      LDOC_WHITESPACE,

      LDOC_DASHES,
      LDOC_TAG_NAME,
      LDOC_TAG_VALUE,

      LDOC_TAG_PLAIN_VALUE_TOKEN,
          
      LDOC_COMMENT_BAD_CHARACTER
  );

}
