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

    IElementType FUNCTION_DEFINITION = new LuaElementType("Function Definition");
    IElementType ANON_FUNCTION_DEFINITION = new LuaElementType("Anonymous Function Definition");

    TokenSet FUNCTION_IDENTIFIER_SET = TokenSet.create(NAME, DOT, COLON);
    TokenSet IDENTIFIER_SET = TokenSet.create(NAME, DOT);


    IElementType FUNCTION_IDENTIFIER = new LuaElementType("Function identifier");

    IElementType BLOCK = new LuaElementType("Block");

    IElementType FUNCTION_BLOCK = new LuaElementType("Function Block");
    IElementType ANON_FUNCTION_BLOCK = new LuaElementType("Anonymous Function Block");
    IElementType WHILE_BLOCK = new LuaElementType("While Block");
    IElementType GENERIC_FOR_BLOCK = new LuaElementType("Generic For Block");
    IElementType IF_THEN_BLOCK = new LuaElementType("If-Then Block");
    IElementType NUMERIC_FOR_BLOCK = new LuaElementType("Numeric For Block");


    TokenSet BLOCK_SET = TokenSet.create(FUNCTION_BLOCK, ANON_FUNCTION_BLOCK, WHILE_BLOCK,
            GENERIC_FOR_BLOCK, IF_THEN_BLOCK, NUMERIC_FOR_BLOCK);

    TokenSet FOLDABLE_BLOCKS = TokenSet.create(FUNCTION_BLOCK, ANON_FUNCTION_BLOCK);
   

    IElementType BLOCK_BEGIN = new LuaElementType("block begin");
    IElementType BLOCK_END = new LuaElementType("block end");

    
    TokenSet BLOCK_BEGIN_SET = TokenSet.create(WHILE, FOR, DO, FUNCTION, IF, REPEAT);
    TokenSet BLOCK_BEGIN_SET_NODO = TokenSet.create(WHILE, FOR, FUNCTION, IF, REPEAT);
    TokenSet BLOCK_END_SET = TokenSet.create(END, UNTIL);
    IElementType PARAMETERS = new LuaElementType("function parameters");
}
