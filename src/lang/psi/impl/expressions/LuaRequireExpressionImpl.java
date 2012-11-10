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

import com.intellij.lang.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.*;
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.lists.*;
import com.sylvanaar.idea.Lua.lang.psi.resolve.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import org.jetbrains.annotations.*;

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
        PsiElement e = getNameElement();
        if (e == null) return null;

        if (e instanceof LuaLiteralExpression)
            return String.valueOf(((LuaLiteralExpression)e).getValue());

        return null;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("rename of require not implmemented");
    }

    @Override
    public PsiReference getReference() {
        return getRangeInElement() != null ? this : null;
    }

    @Nullable
    public PsiElement resolve() {
        ResolveResult[] results = multiResolve(false);
        for (ResolveResult result : results) {
            log(result.getElement().toString());
        }
        return results.length == 1 ? results[0].getElement() : null;
    }

    private static final LuaResolver RESOLVER = new LuaResolver();

    @NotNull
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        final String refName = getName();
        if (refName == null) return LuaResolveResult.EMPTY_ARRAY;

        return ResolveCache.getInstance(getProject()).resolveWithCaching(this, RESOLVER, true, false);
    }

    @Override
    public PsiElement getElement() {
        return this;
    }

    @NotNull
    @Override
    public LuaType getLuaType() {
        LuaSymbol e = (LuaSymbol) resolve();
        if (e == null) return LuaPrimitiveType.ANY;

        return e.getLuaType();
    }

    public PsiElement getNameElement() {
        LuaExpressionList argumentList = getArgumentList();

        if (argumentList == null) return null;

        return argumentList.getLuaExpressions().get(0);
    }

    @Override
    public TextRange getRangeInElement() {
        PsiElement e = getNameElement();

        if (e instanceof LuaStringLiteralExpressionImpl) {
            LuaStringLiteralExpressionImpl moduleNameElement = (LuaStringLiteralExpressionImpl) e;

            TextRange name = moduleNameElement.getStringContentTextRange();
            if (name == null) return null;

            return name.shiftRight(e.getTextOffset() - getTextOffset());
        }

        return null;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        return getElement();
    }

    //    @Override
    //   public int getStartOffsetInParent() {
//        PsiElement e = getElement();
//        if (e == null) return 0;
//
//        return e.getTextOffset() - getTextOffset();
//    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return StringUtil.notNullize(getName());
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
        return getManager().areElementsEquivalent(element, resolve());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public boolean isSameKind(LuaSymbol symbol) {
        return true;
    }

    @Override
    public boolean isAssignedTo() {
        return false;
    }

    @Override
    public boolean checkSelfReference(PsiElement element) {
        return false;
    }
}
