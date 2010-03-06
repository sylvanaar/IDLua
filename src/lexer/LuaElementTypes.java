/*
 * Copyright 2009 Max Ishchenko
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

package com.sylvanaar.idea.Lua.lexer;

import com.intellij.lang.Language;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.LuaLanguage;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 06.07.2009
 * Time: 15:49:41
 */
public interface LuaElementTypes {

    IFileElementType FILE = new IFileElementType(Language.findInstance(LuaLanguage.class));

    IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;
    IElementType WHITE_SPACE = TokenType.WHITE_SPACE;

    IElementType COMMENT = new LuaElementType("COMMENT");

    //non-lexed types
    IElementType DIRECTIVE = new LuaElementType("DIRECTIVE");
    IElementType CONTEXT = new LuaElementType("CONTEXT");
    IElementType COMPLEX_VALUE = new LuaElementType("COMPLEX_VALUE");

    //lexed types
    IElementType CONTEXT_NAME = new LuaElementType("CONTEXT_NAME");
    IElementType DIRECTIVE_NAME = new LuaElementType("DIRECTIVE_NAME");
    IElementType DIRECTIVE_VALUE = new LuaElementType("DIRECTIVE_VALUE");
    IElementType DIRECTIVE_STRING_VALUE = new LuaElementType("DIRECTIVE_STRING_VALUE");
    IElementType VALUE_WHITE_SPACE = new LuaElementType("VALUE_WHITE_SPACE");
    IElementType INNER_VARIABLE = new LuaElementType("INNER_VARIABLE");
    IElementType OPENING_BRACE = new LuaElementType("OPENING_BRACE");
    IElementType CLOSING_BRACE = new LuaElementType("CLOSING_BRACE");
    IElementType SEMICOLON = new LuaElementType("SEMICOLON");

    TokenSet WHITE_SPACES = TokenSet.create(WHITE_SPACE);
    TokenSet COMMENTS = TokenSet.create(COMMENT);
    TokenSet STRINGS = TokenSet.create(DIRECTIVE_STRING_VALUE);
}


