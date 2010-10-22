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
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaGenericForStatement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 13, 2010
 * Time: 2:12:45 AM
 */
public class LuaGenericForStatementImpl extends LuaStatementElementImpl implements LuaGenericForStatement {
    public LuaGenericForStatementImpl(ASTNode node) {
        super(node);
    }


    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {

        if (place.getParent().getParent().getParent().getParent() != this ) {
            LuaExpression[] names = getIndices();
            for (LuaExpression name : names) {
                 if (!processor.execute(name, resolveState)) return false;
            }
       }

       return true;
    }

    @Override
    public LuaExpression[] getIndices() {
        LuaExpression[] e = findChildrenByClass(LuaExpression.class);

        e = e.clone();
        e[e.length-1]=null;

        return e;
    }

    @Override
    public LuaExpression getInClause() {
        LuaExpression[] e = findChildrenByClass(LuaExpression.class);

        return e[e.length-1];
    }
}
