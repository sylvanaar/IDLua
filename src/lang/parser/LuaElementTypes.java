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

package com.sylvanaar.idea.Lua.lang.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.lang.lexer.LuaElementType;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.LuaDocElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaStubElementType;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaCompoundIdentifierStub;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaGlobalDeclarationStub;
import com.sylvanaar.idea.Lua.lang.psi.stubs.elements.LuaStubCompoundIdentifierType;
import com.sylvanaar.idea.Lua.lang.psi.stubs.elements.LuaStubGlobalDeclarationType;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalDeclaration;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 3:54:46 PM
 */
public interface LuaElementTypes extends LuaTokenTypes, LuaDocElementTypes {
    IElementType EMPTY_INPUT = new LuaElementType("empty input");

    

    IElementType FUNCTION_DEFINITION = new LuaElementType("Function Definition");

    IElementType LOCAL_NAME = new LuaElementType("local name");
    IElementType LOCAL_NAME_DECL = new LuaElementType("local name declaration");

    IElementType GLOBAL_NAME = new LuaElementType("global name");
  //  IElementType GLOBAL_NAME_DECL = new LuaElementType("global name declaration");
 // IElementType GETTABLE = new LuaElementType("get table");
//IElementType GETSELF = new LuaElementType("get self");
    LuaStubElementType<LuaGlobalDeclarationStub, LuaGlobalDeclaration> GLOBAL_NAME_DECL = new LuaStubGlobalDeclarationType();
    LuaStubElementType<LuaCompoundIdentifierStub, LuaCompoundIdentifier> GETTABLE = new LuaStubCompoundIdentifierType();
    //LuaStubElementType<LuaCompoundIdentifierStub, LuaCompoundIdentifier> GETSELF = new LuaStubCompoundIdentifierType();

    IElementType FIELD_NAME = new LuaElementType("field name");

    

    IElementType TABLE_INDEX = new LuaElementType("table index");
    IElementType KEY_ASSIGNMENT = new LuaElementType("keyed field initializer");
    IElementType IDX_ASSIGNMENT = new LuaElementType("indexed field initializer");

    IElementType REFERENCE = new LuaElementType("Reference");

    IElementType COMPOUND_REFERENCE = new LuaElementType("Compound Reference");
    IElementType IDENTIFIER_LIST = new LuaElementType("Identifier List");

    IElementType STATEMENT = new LuaElementType("Statment");
    IElementType LAST_STATEMENT = new LuaElementType("LastStatement");
    IElementType EXPR = new LuaElementType("Expression");
    IElementType EXPR_LIST = new LuaElementType("Expression List");

    IElementType LITERAL_EXPRESSION = new LuaElementType("Literal Expression");
    IElementType PARENTHEICAL_EXPRESSION = new LuaElementType("Parentheical Expression");

    IElementType TABLE_CONSTUCTOR = new LuaElementType("Table Constructor");
    IElementType FUNCTION_CALL_ARGS = new LuaElementType("Function Call Args");
    IElementType FUNCTION_CALL = new LuaElementType("Function Call Statement");
    IElementType FUNCTION_CALL_EXPR = new LuaElementType("Function Call Expression");
    IElementType ANONYMOUS_FUNCTION_EXPRESSION = new LuaElementType("Anonymous function expression");

    IElementType ASSIGN_STMT = new LuaElementType("Assignment Statement");
    IElementType CONDITIONAL_EXPR = new LuaElementType("Conditional Expression");

    IElementType LOCAL_DECL_WITH_ASSIGNMENT = new LuaElementType("Local Declaration With Assignment Statement");
    IElementType LOCAL_DECL = new LuaElementType("Local Declaration ");

    IElementType SELF_PARAMETER = new LuaElementType("Implied parameter (self)");

    IElementType BLOCK = new LuaElementType("Block");

    IElementType UNARY_EXP = new LuaElementType("UnExp");
    IElementType BINARY_EXP = new LuaElementType("BinExp");
    IElementType UNARY_OP = new LuaElementType("UnOp");
    IElementType BINARY_OP = new LuaElementType("BinOp");

    IElementType DO_BLOCK = new LuaElementType("Do Block");

    IElementType WHILE_BLOCK = new LuaElementType("While Block");

    IElementType REPEAT_BLOCK = new LuaElementType("Repeat Block");
    IElementType GENERIC_FOR_BLOCK = new LuaElementType("Generic For Block");
    IElementType IF_THEN_BLOCK = new LuaElementType("If-Then Block");
    IElementType NUMERIC_FOR_BLOCK = new LuaElementType("Numeric For Block");

    TokenSet EXPRESSION_SET = TokenSet.create(LITERAL_EXPRESSION, BINARY_EXP, UNARY_EXP, EXPR);
    IElementType RETURN_STATEMENT = new LuaElementType("Return statement");
    IElementType RETURN_STATEMENT_WITH_TAIL_CALL = new LuaElementType("Tailcall Return statement");

    IElementType LOCAL_FUNCTION = new LuaElementType("local function def");

    TokenSet BLOCK_SET = TokenSet.create(FUNCTION_DEFINITION, LOCAL_FUNCTION, ANONYMOUS_FUNCTION_EXPRESSION,
            WHILE_BLOCK,
            GENERIC_FOR_BLOCK,
            IF_THEN_BLOCK,
            NUMERIC_FOR_BLOCK,
            REPEAT_BLOCK,
            DO_BLOCK);

    IElementType PARAMETER = new LuaElementType("function parameters");
    IElementType PARAMETER_LIST = new LuaElementType("function parameter");

    IElementType UPVAL_NAME = new LuaElementType("upvalue name");
}
