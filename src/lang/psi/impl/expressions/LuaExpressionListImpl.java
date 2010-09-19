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

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 13, 2010
 * Time: 7:18:16 AM
 */
public class LuaExpressionListImpl extends LuaExpressionImpl implements LuaExpressionList {
    public LuaExpressionListImpl(ASTNode node) {
        super(node);
    }

    @Override
    public int count() {
        return getLuaExpressions().size();
    }


    public List<LuaExpression> getLuaExpressions() {
        return Arrays.asList(findChildrenByClass(LuaExpression.class));
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {

       // log.info("decls " + this);
        final PsiElement[] children = getChildren();
        for (PsiElement child : children) {
            if (child == lastParent) break;
            if (!child.processDeclarations(processor, resolveState, lastParent, place)) return false;
        }
        return true;
    }
}
