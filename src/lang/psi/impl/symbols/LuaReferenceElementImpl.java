package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolver;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobal;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * TODO: implement all reference stuff...
 */
public abstract class LuaReferenceElementImpl extends LuaSymbolImpl implements LuaReferenceElement {
    public LuaReferenceElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitReferenceElement(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitReferenceElement(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public Object evaluate() {
        return null;
    }

    public PsiElement getElement() {
        return this;
    }

    public PsiReference getReference() {
        return this;
    }
    @Override
    public LuaType getLuaType() {
        assert getElement() instanceof LuaExpression;

        final PsiElement element = getElement();
        if (element == this) super.getLuaType();

        return ((LuaExpression) getElement()).getLuaType();
    }

    @Override
    public void setLuaType(LuaType type) {
        final PsiElement element = getElement();
        if (element == this) super.setLuaType(type);

        ((LuaSymbol) element).setLuaType(type);
    }

    @Override
    @NotNull
    public PsiReference[] getReferences() {
        return super.getReferences();
    }


    @SuppressWarnings("UnusedDeclaration")
    public PsiElement getResolvedElement() {
        return resolve();
    }



    public TextRange getRangeInElement() {
        final PsiElement nameElement = getElement();
        return new TextRange(getTextOffset() - nameElement.getTextOffset(), nameElement.getTextLength());
    }

    @Nullable
    public PsiElement resolve() {
        ResolveResult[] results = ResolveCache.getInstance(getProject()).resolveWithCaching(this, RESOLVER, true, false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    @NotNull
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        return ResolveCache.getInstance(getProject()).resolveWithCaching(this, RESOLVER, true, incompleteCode);
    }

    private static final LuaResolver RESOLVER = new LuaResolver();

    @NotNull
    public String getCanonicalText() {
        final PsiElement element = getElement();
        if (element instanceof LuaGlobal)
            return StringUtil.notNullize(((LuaGlobal) element).getGlobalEnvironmentName(), element.getText());

        return StringUtil.notNullize(getName(), element.getText());
    }

     public PsiElement setName(@NotNull String s) {
        ((PsiNamedElement)getElement()).setName(s);

        resolve();

        return this;
     }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        ((PsiNamedElement)getElement()).setName(newElementName);
        resolve();
        return this;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        final LuaIdentifier identifier = findChildByClass(LuaIdentifier.class);
        if (identifier == null) throw new IncorrectOperationException("Cant bind to non-identifier");
        identifier.replace(element);
        return this;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        //return getElement().getManager().areElementsEquivalent(element, resolve());

        return element == resolve();
    }

    @NotNull
    public Object[] getVariants() {
        return ResolveUtil.getVariants(this);
    }

    public boolean isSoft() {
        return false;
    }

    public boolean isAssignedTo() {
        return false;
    }

    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return LuaPsiUtils.replaceElement(this, newCall);
    }

    @Override
    public String getName() {
        return ((PsiNamedElement)getElement()).getName();
    }
}