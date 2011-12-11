/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
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
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.reference.SoftReference;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementFactoryImpl;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocal;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 3, 2010
 * Time: 12:38:19 AM
 */
public class LuaLocalDeclarationImpl extends LuaPsiDeclarationReferenceElementImpl implements
        LuaDeclarationExpression, LuaLocalDeclaration {
    public LuaLocalDeclarationImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitDeclarationExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitDeclarationExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public String getDefinedName() {
        return getName();
    }


    @Override
    public PsiElement setName(@NotNull String s) {
        LuaDeclarationExpression decl =
                LuaPsiElementFactoryImpl.getInstance(getProject()).createLocalNameIdentifierDecl(s);

        return replace(decl);
    }

    @Override
    public String toString() {
        return "Local Decl: " + getDefinedName();
    }

    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        return GlobalSearchScope.fileScope(this.getContainingFile());
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return new LocalSearchScope(getContainingFile());
    }


    @Override
    public boolean isSameKind(LuaSymbol identifier) {
        return identifier instanceof LuaLocal;
    }

    SoftReference<LuaExpression> myAlias = null;

    @Override
    public LuaExpression getAliasElement() {
        return myAlias.get();
    }

    @Override
    public void setAliasElement(@Nullable LuaExpression element) {
        myAlias = new SoftReference<LuaExpression>(element);
    }

    @Override
    public PsiReference getReference() {
        if (getParent() instanceof PsiReference && ((PsiReference) getParent()).getElement().equals(this))
            return (PsiReference) getParent();

        return super.getReference();
    }
}
