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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lexer.LuaElementType;
import com.sylvanaar.idea.Lua.psi.impl.*;
import com.sylvanaar.idea.Lua.psi.impl.statements.*;

import static com.sylvanaar.idea.Lua.parser.LuaElementTypes.*;

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

        if (node.getElementType() == FUNCTION_CALL)
            return new LuaFunctionCallStatementImpl(node);


        if (node.getElementType() == WHILE_BLOCK)
            return new LuaWhileStatementImpl(node);

        if (node.getElementType() == ASSIGN_STMT)
            return new LuaAssignmentStatementImpl(node);

        if (node.getElementType() == DO_BLOCK)
            return new LuaDoStatementImpl(node);

        if (node.getElementType() == IF_THEN_BLOCK)
            return new LuaIfThenStatementImpl(node);
        if (node.getElementType() == FUNCTION_IDENTIFIER)
            return new LuaIdentifierImpl(node);

        if (node.getElementType() == GLOBAL_NAME)
            return new LuaIdentifierImpl(node);

        if (node.getElementType() == LOCAL_NAME)
            return new LuaIdentifierImpl(node);

        if (node.getElementType() == FIELD_NAME)
            return new LuaIdentifierImpl(node);

        if (node.getElementType() == FUNCTION_DEFINITION)
            return new LuaFunctionImpl(node);

        if (node.getElementType() == LuaElementTypes.PARAMETER_LIST)
            return new LuaParameterListImpl(node);

        if (node.getElementType() == LuaElementTypes.PARAMETER)
            return new LuaParameterImpl(node);

        if (GENERIC_CODE_BLOCKS.contains(node.getElementType()))
            return new LuaCodeBlockImpl(node);

        return new LuaPsiElementImpl(node);
    }

}
