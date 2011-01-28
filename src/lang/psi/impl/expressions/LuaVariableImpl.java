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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiType;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
* Created by IntelliJ IDEA.
* User: Jon S Akhtar
* Date: Jun 14, 2010
* Time: 11:23:33 PM
*/
public class LuaVariableImpl extends LuaPsiElementImpl implements LuaVariable {
    public LuaVariableImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "Variable: " + getText();
    }

    @Override
    public PsiElement resolve() {
        PsiElement e = getFirstChild();
        if (e instanceof LuaIdentifier)
            return e;

        if (e instanceof LuaReferenceExpression)
            return ((LuaReferenceExpression) e).resolve();

        return this;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return null; 
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return false;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitCompoundReferenceExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitCompoundReferenceExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return null;
    }

    @Override
    public LuaPsiType getType() {
        return null;
    }

    @Override
    public PsiElement getElement() {
        return this;
    }

    @Override
    public LuaNamedElement getPrimaryIdentifier() {

        LuaNamedElement e = findChildByClass(LuaDeclarationExpression.class);
        if (e!=null) return e;

        LuaReferenceExpression r =findChildByClass(LuaReferenceExpression.class);

        if (r!=null)
        return (LuaNamedElement) r.getElement();

        return null;
    }

    @Override
    public LuaIdentifier reduceToIdentifier() {
        PsiElement e = getFirstChild();

        while (e != null) {
            if (e instanceof LuaIdentifier)
                return (LuaIdentifier) e;

            if (e instanceof LuaReferenceExpression)
                e = ((LuaReferenceExpression)e).getElement();

            if (e instanceof LuaVariable)
                return null;
        }

        return null;
    }

    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        LuaNamedElement id = getPrimaryIdentifier();
        return id!=null?id.getResolveScope():GlobalSearchScope.EMPTY_SCOPE;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
         LuaNamedElement id = getPrimaryIdentifier();
         return id!=null?id.getUseScope():super.getUseScope();
    }

    @Override
    public boolean isSameKind(LuaSymbol symbol) {
        LuaIdentifier i = reduceToIdentifier();

        if (i != null)
            return i.isSameKind(symbol);

        return symbol instanceof LuaVariable;
    }

    @Override
    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


     public TextRange getRangeInElement() {
        final LuaNamedElement id = getPrimaryIdentifier();
        final ASTNode nameElement = id!=null?id.getNode():null;
        final int startOffset = nameElement != null ? nameElement.getStartOffset() : getNode().getTextRange().getEndOffset();
        return new TextRange(startOffset - getNode().getStartOffset(), getTextLength());
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return new ResolveResult[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
