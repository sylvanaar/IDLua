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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.lexer.LuaElementType;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaLocalDeclarationImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.statements.*;

import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 14, 2010
 * Time: 6:56:50 PM
 */
public class LuaPsiCreator {

    public static PsiElement createElement(ASTNode node) {
        IElementType elem = node.getElementType();

        if (elem instanceof LuaElementType.PsiCreator) {
            return ((LuaElementType.PsiCreator) elem).createPsi(node);
        }

        if (node.getElementType() == EXPR)
            return new LuaExpressionImpl(node);

        if (node.getElementType() == FUNCTION_CALL_EXPR)
            return new LuaFunctionCallExpressionImpl(node);

        if (node.getElementType() == ANONYMOUS_FUNCTION_EXPRESSION)
            return new LuaAnonymousFunctionExpressionImpl(node);

        if (node.getElementType() == CONDITIONAL_EXPR)
            return new LuaConditionalExpressionImpl(node);

        if (node.getElementType() == REFERENCE)
            return new LuaReferenceExpressionImpl(node);

        if (node.getElementType() == TABLE_CONSTUCTOR)
            return new LuaTableConstructorImpl(node);

        if (node.getElementType() == IDX_ASSIGNMENT)
            return new LuaExpressionImpl(node);        
        if (node.getElementType() == KEY_ASSIGNMENT)
            return new LuaKeyValueInitializerImpl(node);

        if (node.getElementType() == BLOCK)
            return new LuaBlockImpl(node);
        if (node.getElementType() == REPEAT_BLOCK)
            return new LuaRepeatStatementImpl(node);
        
        if (node.getElementType() == LOCAL_DECL)
            return new LuaLocalDefinitionStatementImpl(node);

        if (node.getElementType() == LOCAL_DECL_WITH_ASSIGNMENT)
            return new LuaLocalDefinitionStatementImpl(node);

        if (node.getElementType() == EXPR_LIST)
            return new LuaExpressionListImpl(node);

        if (node.getElementType() == IDENTIFIER_LIST)
            return new LuaIdentifierListImpl(node);

        if (node.getElementType() == VARIABLE)
            return new LuaVariableImpl(node);
        
        if (node.getElementType() == LITERAL_EXPRESSION)
            return new LuaLiteralExpressionImpl(node);

        if (node.getElementType() == BINARY_EXP)
            return new LuaBinaryExpressionImpl(node);
        if (node.getElementType() == UNARY_EXP)
            return new LuaUnaryExpressionImpl(node);

        if (node.getElementType() == FUNCTION_CALL)
            return new LuaFunctionCallStatementImpl(node);

        if (node.getElementType() == RETURN_STATEMENT ||
                node.getElementType() == RETURN_STATEMENT_WITH_TAIL_CALL)
            return new LuaReturnStatementImpl(node);

        if (node.getElementType() == NUMERIC_FOR_BLOCK)
            return new LuaNumericForStatementImpl(node);

        if (node.getElementType() == PARENTHEICAL_EXPRESSION)
            return new LuaParenthesizedExpressionImpl(node);

        if (node.getElementType() == GENERIC_FOR_BLOCK)
            return new LuaGenericForStatementImpl(node);

        if (node.getElementType() == WHILE_BLOCK)
            return new LuaWhileStatementImpl(node);

        if (node.getElementType() == ASSIGN_STMT)
            return new LuaAssignmentStatementImpl(node);

        if (node.getElementType() == DO_BLOCK)
            return new LuaDoStatementImpl(node);

        if (node.getElementType() == IF_THEN_BLOCK)
            return new LuaIfThenStatementImpl(node);

        if (node.getElementType() == SELF_PARAMETER)
            return new LuaImpliedSelfParameterImpl(node);

        if (node.getElementType() == GLOBAL_NAME) {
            LuaGlobalIdentifier g= new LuaGlobalIdentifierImpl(node);

        if (node.getElementType() == GLOBAL_NAME)
            return new LuaGlobalIdentifierImpl(node);

        if (node.getElementType() == LOCAL_NAME_DECL)
            return new LuaLocalDeclarationImpl(node);

        if (node.getElementType() == LOCAL_NAME)
            return new LuaLocalIdentifierImpl(node);

        if (node.getElementType() == FIELD_NAME)
            return new LuaFieldIdentifierImpl(node);

        if (node.getElementType() == FUNCTION_DEFINITION )
            return new LuaFunctionDefinitionStatementImpl(node);
        if (node.getElementType() == LOCAL_FUNCTION)
            return new LuaLocalFunctionDefinitionStatementImpl(node);
        if (node.getElementType() == LuaElementTypes.PARAMETER_LIST)
            return new LuaParameterListImpl(node);

        if (node.getElementType() == LuaElementTypes.PARAMETER)
            return new LuaParameterImpl(node);

        if (node.getElementType() == LuaElementTypes.FUNCTION_CALL_ARGS)
            return new LuaFunctionArgumentsImpl(node);

        if (node.getElementType() == LuaElementTypes.GETTABLE)
            return new LuaGetTableExpressionImpl(node);

        return new LuaPsiElementImpl(node);
    }

}
