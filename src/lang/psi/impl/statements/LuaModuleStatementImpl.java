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

package com.sylvanaar.idea.Lua.lang.psi.impl.statements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaLiteralExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaModuleStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/7/11
 * Time: 11:21 AM
 */
public class LuaModuleStatementImpl extends LuaFunctionCallStatementImpl implements LuaModuleStatement {
    public LuaModuleStatementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "Module: " + (getName()!=null?getName():"err");
    }


    public String getName() {
        LuaLiteralExpression lit = (LuaLiteralExpression) getInvokedExpression().getArgumentList().getLuaExpressions().get(0);

        if (lit != null && lit.getLuaType() == LuaType.STRING) {
            return lit.getText();
            //return (String) lit.getValue();
        }

        return null;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        return null; 
    }

    @Override
    public String getDefinedName() {
        return getName();
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {

        processor.execute(this, resolveState);

        return true;
    }


    @Override
    public boolean isSameKind(LuaSymbol symbol) {
        return symbol instanceof LuaGlobalIdentifier || symbol instanceof LuaCompoundIdentifier;
    }

    @Override
    public boolean isAssignedTo() {
        return true; 
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        
        return null;
    }

    @Override
    public LuaType getLuaType() {
        return LuaType.TABLE;
    }
}
