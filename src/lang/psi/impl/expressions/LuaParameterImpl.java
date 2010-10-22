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
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiType;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaDeclarationImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementFactoryImpl;
import com.sylvanaar.idea.Lua.lang.psi.util.ResolveUtil;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.sylvanaar.idea.Lua.lang.psi.LuaPsiType.VOID;


public class LuaParameterImpl extends LuaDeclarationImpl implements LuaPsiElement, LuaParameter {
    public LuaParameterImpl(@NotNull ASTNode node) {
        super(node);
    }

    public String toString() {
        return "Parameter: " + getText();
    }

    @Override
    public LuaFunctionDefinition getDeclaringFunction() {
        return (LuaFunctionDefinition) getNode().getTreeParent().getTreeParent().getPsi();

    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        super.accept(visitor);
        visitor.visitParameter(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        super.accept(visitor);
        
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitParameter(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public boolean isVarArgs() {
        return (getNode().getElementType() == LuaElementTypes.ELLIPSIS);
    }

    @NotNull
    @Override
    public LuaPsiType getType() {
        return VOID;
    }


    @NotNull
    public SearchScope getUseScope() {
//        if (!isPhysical()) {
//            final PsiFile file = getContainingFile();
//            final PsiElement context = file.getContext();
//            if (context != null) return new LocalSearchScope(context);
//            return super.getUseScope();
//        }

        final PsiElement scope = getDeclarationScope();

        return new LocalSearchScope(scope);
    }

    @NotNull
    public PsiElement getDeclarationScope() {
        return getDeclaringFunction();
    }

//    @Override
//    public PsiElement setName(@NotNull @NonNls String s) throws IncorrectOperationException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }


    @Override
    public LuaIdentifier getNameSymbol() {
        return this;
    }

    @Override
    public String getDefinedName() {
        return getName();
    }


      @Override
    public String getName() {
        return getText();
    }


    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public PsiElement setName(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nullable
    public String getReferencedName() {
        final ASTNode nameElement = getNameElement();
        return nameElement != null ? nameElement.getText() : null;
    }

    public PsiElement getElement() {
        return this;
    }

    public PsiReference getReference() {
        return this;
    }

    public TextRange getRangeInElement() {
        final ASTNode nameElement = getNameElement();
        final int startOffset = nameElement != null ? nameElement.getStartOffset() : getNode().getTextRange().getEndOffset();
        return new TextRange(startOffset - getNode().getStartOffset(), getTextLength());
    }

    private ASTNode getNameElement() {
        PsiElement e = findChildByClass(LuaIdentifierImpl.class);

        if (e != null)
            return e.getNode();

        return null;
    }

    public PsiElement resolve() {
        final String referencedName = getReferencedName();
        if (referencedName == null) return null;

//        if (getQualifier() != null) {
//            return null; // TODO?
//        }

        return ResolveUtil.treeWalkUp(new ResolveUtil.ResolveProcessor(referencedName), this, this, this);
    }

    public String getCanonicalText() {
        return null;
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        final ASTNode nameElement = LuaPsiElementFactoryImpl.getInstance(getProject()).createNameIdentifier(newElementName);
        getNode().replaceChild(getNameElement(), nameElement);
        return this;
    }

    public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
        final ASTNode nameElement = LuaPsiElementFactoryImpl.getInstance(getProject()).createNameIdentifier(((PsiNamedElement) element).getName());
        getNode().replaceChild(getNameElement(), nameElement);
        return this;
    }

    public boolean isReferenceTo(PsiElement element) {
        if (element instanceof PsiNamedElement) {
            if (Comparing.equal(getReferencedName(), ((PsiNamedElement) element).getName()))
                return resolve() == element;
        }
        return false;
    }
    @NotNull
    public Object[] getVariants() {
        final ResolveUtil.VariantsProcessor processor = new ResolveUtil.VariantsProcessor();
        ResolveUtil.treeWalkUp(processor, this, this, this);
        return processor.getResult();
    }

    public boolean isSoft() {
        return false;
    }

    @Override
    public LuaReferenceExpression getPrimaryIdentifier() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
