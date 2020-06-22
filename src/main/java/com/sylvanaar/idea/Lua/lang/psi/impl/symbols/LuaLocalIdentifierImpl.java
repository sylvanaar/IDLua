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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementFactoryImpl;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolver;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaAssignment;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/15/11
 * Time: 1:29 AM
 */
public class LuaLocalIdentifierImpl extends LuaIdentifierImpl implements LuaLocalIdentifier, LuaReferenceElement {
    public LuaLocalIdentifierImpl(ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement setName(@NotNull String s) throws IncorrectOperationException {
        LuaIdentifier node = LuaPsiElementFactoryImpl.getInstance(getProject()).createLocalNameIdentifier(s);
        replace(node);

        final PsiReference reference = node.getReference();
        if (reference != null) reference.resolve();

        return this;
    }


    @Override
    public boolean isSameKind(LuaSymbol identifier) {
        if (isAssignedTo())
            return identifier instanceof LuaLocalIdentifier;

        return identifier instanceof LuaLocalDeclaration;
    }

    @Override
    public boolean isAssignedTo() {
        PsiElement parent = getParent();
        while (!(parent instanceof LuaStatementElement)) {
            parent = parent.getParent();
        }

        if (parent instanceof LuaAssignmentStatement) {
            LuaAssignmentStatement s = (LuaAssignmentStatement)parent;

            for (LuaAssignment assignment : s.getAssignments())
                if (assignment.getSymbol() == this)
                    return true;
        }

        return false;
    }

    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        return GlobalSearchScope.fileScope(this.getContainingFile());
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return getResolveScope();
    }

    @Override
    public PsiReference getReference() {
        return this;
    }


    public PsiElement getResolvedElement() {
        return getReference().resolve();
    }


    @Override
    public String toString() {
        return "Local: " + getText();
    }

    @Override
    public LuaLocalIdentifier getNamedElement() {
        return this;
    }

    @Override
    public boolean checkSelfReference(PsiElement element) {
        return element != this;
    }

    private static final ResolveResult[] EMPTY_RESULTS = new ResolveResult[0];

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        final PsiElement element = this;
        if (checkSelfReference(element)) return EMPTY_RESULTS;

        final Project project = getProject();
        if (project.isDisposed()) return EMPTY_RESULTS;

        assert isValid() : "resolving invalid element " + this;

        return ResolveCache.getInstance(project).resolveWithCaching(this, RESOLVER, true, incompleteCode);
    }

    @NotNull
    @Override
    public PsiElement getElement() {
        return this;
    }

    @NotNull
    @Override
    public TextRange getRangeInElement() {
        return TextRange.create(0, getTextLength());
    }

    private static final LuaResolver RESOLVER = new LuaResolver();

    @Nullable
    @Override
    public PsiElement resolve() {
        final PsiElement element = getNamedElement();
        if (checkSelfReference(element)) return element;

        final Project project = getProject();
        if (project.isDisposed()) return null;

        assert isValid() : "resolving invalid element " + this;

        ResolveResult[] results = ResolveCache.getInstance(project).resolveWithCaching(this, RESOLVER, true, false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return getText();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        setName(newElementName);
        resolve();
        return this;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        replace(element);
        return this;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return getElement().getManager().areElementsEquivalent(resolve(), element);
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return super.isEquivalentTo(another);
    }

    @Override
    public boolean isSoft() {
        return false;
    }
}
