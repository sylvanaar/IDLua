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
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaNumericForStatement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 13, 2010
 * Time: 2:11:33 AM
 */
public class LuaNumericForStatementImpl extends LuaStatementElementImpl implements LuaNumericForStatement {
    public LuaNumericForStatementImpl(ASTNode node) {
        super(node);
    }


    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {

        PsiElement parent = place.getParent();
        while (parent != null && !(parent instanceof LuaPsiFile)) {
            if (parent == getBlock()) {
                if (!processor.execute(getIndex(), resolveState)) return false;
            }

            parent = parent.getParent();
        }

        return true;
    }

    @Override
    public LuaExpression getIndex() {
        return findChildrenByClass(LuaExpression.class)[0];
    }

    @Override
    public LuaExpression getStart() {
        return findChildrenByClass(LuaExpression.class)[1];
    }

    @Override
    public LuaExpression getEnd() {
        return findChildrenByClass(LuaExpression.class)[2];
    }

    @Override
    public LuaExpression getStep() {
        LuaExpression[] e = findChildrenByClass(LuaExpression.class);

        if (e.length >= 4)
            return e[3];

        return null;
    }

    @Override
    public LuaBlock getBlock() {
        return findChildByClass(LuaBlock.class);
    }


    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitNumericForStatement(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitNumericForStatement(this);
        } else {
            visitor.visitElement(this);
        }
    }


}
