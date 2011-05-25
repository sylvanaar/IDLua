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

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaRequireExpression;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/7/11
 * Time: 11:19 AM
 */
public class LuaRequireExpressionImpl extends LuaFunctionCallExpressionImpl implements LuaRequireExpression, LuaReferenceElement {
    public LuaRequireExpressionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "Require Expr: " + StringUtil.notNullize(getName());
    }

   @Override
    public String getName() {
        PsiElement e = getElement();
        if (e == null) return null;

        return LuaStringLiteralExpressionImpl.stripQuotes(e.getText());
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("rename of require not implmemented");
    }

    @Override
    public PsiReference getReference() {
        return this;
    }

    @Override
    public PsiElement resolveWithoutCaching(boolean ingnoreAlias) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return new ResolveResult[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement getElement() {
        LuaExpressionList argumentList = getArgumentList();

        if (argumentList == null) return null;

        return  argumentList.getLuaExpressions().get(0);
    }

    @Override
    public TextRange getRangeInElement() {
        PsiElement e = getElement();

        if (e instanceof LuaStringLiteralExpressionImpl) {
            LuaStringLiteralExpressionImpl moduleNameElement = (LuaStringLiteralExpressionImpl) e;

            return moduleNameElement.getStringContentTextRange();
        }

        return null;
    }

    @Override
    public int getStartOffsetInParent() {
        PsiElement e = getElement();
        if (e == null) return 0;
        
        return e.getTextOffset() - getTextOffset();
    }

    @Override
    public PsiElement resolve() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return getText();
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSoft() {
        return true;
    }
}
