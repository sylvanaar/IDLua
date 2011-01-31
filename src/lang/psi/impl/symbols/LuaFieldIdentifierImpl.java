/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaGetTableExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaVariable;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/15/11
 * Time: 1:31 AM
 */
public class LuaFieldIdentifierImpl  extends LuaReferenceElementImpl implements LuaFieldIdentifier {
    public LuaFieldIdentifierImpl(ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiReference getReference() {
        return getCompositeIdentifier();
    }

    @Override
    public boolean isSameKind(LuaSymbol identifier) {
        return identifier instanceof LuaFieldIdentifier || identifier instanceof LuaVariable;
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitIdentifier(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitIdentifier(this);
        } else {
            visitor.visitElement(this);
        }
    }

    public boolean  isDeclaration() {
        return isAssignedTo();
    }

    @Override
    public boolean isAssignedTo() {
        LuaVariable v = getCompositeIdentifier();

        if (v == null)
            return true; // the only times fields are not part of a composite identifier are table constructors.

        return false;//v.isAssignedTo();
    }

    public LuaVariable getCompositeIdentifier() {
        PsiElement s = this;

        while (s.getParent() instanceof LuaGetTableExpression) {
            s = s.getParent();
        }

        if (s instanceof LuaVariable)
            return (LuaVariable) s;

        return null;
    }
    @Override
    public String toString() {
        return "Field: " + getText();
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState state, PsiElement lastParent,
                                       @NotNull PsiElement place) {
        if (isDeclaration()) {
            if (!processor.execute(this,state)) return false;
        }

        return super.processDeclarations(processor, state, lastParent, place);
    }


}
