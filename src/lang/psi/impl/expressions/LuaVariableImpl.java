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
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiType;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaVariable;

import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
* Created by IntelliJ IDEA.
* User: Jon S Akhtar
* Date: Jun 14, 2010
* Time: 11:23:33 PM
*/
public class LuaVariableImpl extends LuaReferenceExpressionImpl implements LuaVariable {
    public LuaVariableImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "Variable: " + getText();
    }

    @Override
    public PsiElement resolve() {
        return null;
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        super.accept(visitor);
        visitor.visitReferenceExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        super.accept(visitor);

        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitReferenceExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public String getCanonicalText() {
        return null;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return false;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public LuaPsiType getType() {
        return null;
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return null;
    }

    @Override
    public boolean isGlobal() {
        LuaIdentifier id = findChildByClass(LuaIdentifier.class);

        if (id == null)
            return false;

        return id.isGlobal();
    }

    @Override
    public boolean isLocal() {
        LuaIdentifier id = findChildByClass(LuaIdentifier.class);

        if (id == null)
            return false;

        return id.isLocal();
    }

    @Override
    public boolean isField() {
        LuaIdentifier id = findChildByClass(LuaIdentifier.class);

        if (id == null)
            return false;

        return id.isField();
    }


    @Override
    public LuaReferenceExpression getPrimaryIdentifier() {
        return findChildByClass(LuaReferenceExpression.class);
    }
}
