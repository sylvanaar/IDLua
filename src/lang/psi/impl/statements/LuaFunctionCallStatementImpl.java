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

package com.sylvanaar.idea.Lua.lang.psi.impl.statements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.JavaResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiMethod;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaExpressionListImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionCallStatement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:40:55 AM
 */
public class LuaFunctionCallStatementImpl extends LuaStatementElementImpl implements LuaFunctionCallStatement {

    public LuaFunctionCallStatementImpl(ASTNode node) {
        super(node);
    }

    LuaFunctionIdentifier identifier;
    public LuaFunctionIdentifier getIdentifier() {
        if (identifier  == null) {
        PsiElement e = findChildByType(LuaElementTypes.FUNCTION_IDENTIFIER_SET);
        if (e != null)
            identifier = (LuaFunctionIdentifier) e;
        }
        return identifier;
    }

    LuaExpressionList parameters;
    public LuaExpressionList getParameters() {
        if (parameters  == null) {
        PsiElement e = findChildByType(LuaElementTypes.PARAMETER_LIST);
        if (e != null)
            parameters = (LuaExpressionListImpl) e;
        }
        return parameters;
    }


    @Override
    public PsiExpressionList getArgumentList() {
        return getParameters();
    }

    @Override
    public PsiMethod resolveMethod() {
        return null;
    }

    @NotNull
    @Override
    public JavaResolveResult resolveMethodGenerics() {
        return null;
    }
}
