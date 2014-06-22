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
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaLiteralExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaRequireExpression;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaRequireResolver;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResult;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResultImpl;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaPrimitiveType;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        throw new IncorrectOperationException("rename of require not implemented");
    }

    @Override
    public PsiReference getReference() {
        return getRangeInElement() != null ? this : null;
    }

    @Nullable
    public PsiElement resolve() {
        return ResolveCache.getInstance(getProject()).resolveWithCaching(this, RESOLVER, true, false);
    }

    private static final LuaRequireResolver RESOLVER = new LuaRequireResolver();

    @NotNull
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        final PsiElement element = resolve();
        if (element == null)
            return LuaResolveResult.EMPTY_ARRAY;

        return new LuaResolveResult[] { new LuaResolveResultImpl(element, true) };
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
            LuaStringLiteralExpressionImpl moduleNameElement = (LuaStringLiteralExpressionImpl)e;

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
