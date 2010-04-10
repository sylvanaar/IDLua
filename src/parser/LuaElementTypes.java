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

package com.sylvanaar.idea.Lua.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.lexer.LuaElementType;
import com.sylvanaar.idea.Lua.lexer.LuaTokenTypes;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 3:54:46 PM
 */
public interface LuaElementTypes extends LuaTokenTypes {
    IElementType EMPTY_INPUT = new LuaElementType("empty input");

    IElementType BLOCK = new LuaElementType("Block");
    IElementType BLOCK_BEGIN = new LuaElementType("block begin");
    IElementType BLOCK_END = new LuaElementType("block end");

    TokenSet BLOCK_BEGIN_SET = TokenSet.create(DO, FUNCTION, IF, REPEAT);
    TokenSet BLOCK_END_SET = TokenSet.create(END, UNTIL);
}
