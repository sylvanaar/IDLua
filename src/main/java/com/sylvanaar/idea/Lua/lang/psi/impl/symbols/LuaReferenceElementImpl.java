package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.*;
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.resolve.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.util.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import org.jetbrains.annotations.*;


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


    @NotNull
    @Override
    public LuaType getLuaType() {
        if (!isValid()) return LuaPrimitiveType.ANY;

        assert getNamedElement() instanceof LuaExpression;


        LuaSymbol s = (LuaSymbol) resolve();
        if (s != null && s != this) {
            setLuaType(s.getLuaType());
            return s.getLuaType();
        }
        
        final PsiElement element = getNamedElement();
        if (element == this) return super.getLuaType();

        return ((LuaExpression) getNamedElement()).getLuaType();
    }

    @Override
    public void setLuaType(LuaType type) {
        final PsiElement element = getNamedElement();
        if (element == this)
            super.setLuaType(type);
        else
            ((LuaSymbol) element).setLuaType(type);
    }

    @Override
    public PsiReference getReference() {
        return this;
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
//        PsiElement element = resolve();
//        PsiElement aliasElement = null;
//        PsiReference aliasReference = null;
//
//        if (element instanceof LuaLocalDeclaration) {
//            aliasElement = ((LuaLocalDeclaration) element).getAliasElement();
//
//            if (aliasElement != null && aliasElement.getReference() != null) {
//                aliasReference = aliasElement.getReference();
//            }
//        }
//
//        return aliasReference != null ? new PsiReference[]{getReference(), aliasReference} : new PsiReference[]{getReference()};
//
    return new PsiReference[]{getReference()};
    }

    @SuppressWarnings("UnusedDeclaration")
    public PsiElement getResolvedElement() {
        return resolve();
    }


    public TextRange getRangeInElement() {
        final PsiElement nameElement = getElement();
        final int textOffset = nameElement.getTextOffset();
        return TextRange.from(textOffset - getTextOffset(), nameElement.getTextLength());
    }

    @Nullable
    public PsiElement resolve() {
        final PsiElement element = getNamedElement();
        if (checkSelfReference(element)) return element;

        final Project project = getProject();
        if (project.isDisposed()) return null;

        assert isValid() : "resolving invalid element " + this;
        if (!isValid()) return null;
        ResolveResult[] results = ResolveCache.getInstance(project).resolveWithCaching(this, RESOLVER, true, false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    @Override
    public boolean checkSelfReference(PsiElement element) {
        return element instanceof LuaDeclarationExpression;
    }

    public PsiElement resolveWithoutCaching() {
        ResolveResult[] results = RESOLVER.resolve(this, false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    @NotNull
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        final Project project = getProject();
        if (project.isDisposed()) return null;

        assert isValid() : "resolving invalid element " + this;
        if (!isValid()) return null;
        return ResolveCache.getInstance(getProject()).resolveWithCaching(this, RESOLVER, true, incompleteCode);
    }

    private static final LuaResolver RESOLVER = new LuaResolver();

    @NotNull
    public String getCanonicalText() {
        final PsiElement element = getNamedElement();
        if (element instanceof LuaGlobal)
            return StringUtil.notNullize(((LuaGlobal) element).getGlobalEnvironmentName(), element.getText());

        return StringUtil.notNullize(getName(), element.getText());
    }

     public PsiElement setName(@NotNull String s) {
        ((PsiNamedElement)getNamedElement()).setName(s);

        resolve();

        return this;
     }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        ((PsiNamedElement)getNamedElement()).setName(newElementName);
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
        return ((PsiNamedElement)getNamedElement()).getName();
    }
}