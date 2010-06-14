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

package com.sylvanaar.idea.Lua.lang.psi.impl.statements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameterList;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaParameterListImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlockStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:40:55 AM
 */
public class LuaFunctionDefinitionStatementImpl extends LuaStatementElementImpl implements LuaFunctionDefinitionStatement, PsiModifierList {
    private LuaParameterList parameters = null;
    private LuaFunctionIdentifier identifier = null;
    private LuaBlockStatement block = null;
    
    public LuaFunctionDefinitionStatementImpl(ASTNode node) {
        super(node);

        assert getBlock() != null;
    }

    public void accept(LuaElementVisitor visitor) {
      visitor.visitFunctionDef(this);
    }


    @Override
    public LuaFunctionIdentifier getIdentifier() {
        if (identifier  == null) {
        PsiElement e = findChildByType(LuaElementTypes.FUNCTION_IDENTIFIER_SET);
        if (e != null)
            identifier = (LuaFunctionIdentifier) e;
        }
        return identifier;
    }

    @Override
    public LuaParameterList getParameters() {
        if (parameters  == null) {
        PsiElement e = findChildByType(LuaElementTypes.PARAMETER_LIST);
        if (e != null)
            parameters = (LuaParameterListImpl) e;
        }
        return parameters;
    }

   public LuaBlockStatement getBlock() {
        if (block  == null) {
        PsiElement e = findChildByType(LuaElementTypes.BLOCK);
        if (e != null)
            block = (LuaBlockStatementImpl) e;
        }
        return block;
    }


    @Override
    public String toString() {
        return "Function Declaration ("+getIdentifier()+")";
    }

    @Override
    public PsiType getReturnType() {
        return PsiType.VOID;
    }

    @Override
    public PsiTypeElement getReturnTypeElement() {
        return null;
    }

    @NotNull
    @Override
    public PsiParameterList getParameterList() {
        return getParameters();
    }

    @NotNull
    @Override
    public PsiReferenceList getThrowsList() {
        return null;
    }

    @Override
    public PsiCodeBlock getBody() {
        if (getBlock() != null)
            return getBlock().getCodeBlock();

        return null;
    }

    @Override
    public boolean isConstructor() {
        return false;
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @NotNull
    @Override
    public MethodSignature getSignature(@NotNull PsiSubstitutor substitutor) {
        return new MethodSignature() {

            @NotNull
            @Override
            public PsiSubstitutor getSubstitutor() {
                return null;
            }

            @NotNull
            @Override
            public String getName() {
                return getIdentifier().getName();
            }

            @NotNull
            @Override
            public PsiType[] getParameterTypes() {
                return new PsiType[0];
            }

            @NotNull
            @Override
            public PsiTypeParameter[] getTypeParameters() {
                return new PsiTypeParameter[0];
            }

            @Override
            public boolean isRaw() {
                return false;
            }
        };
    }

    @Override
    public PsiIdentifier getNameIdentifier() {
        return getIdentifier();
    }

    @NotNull
    @Override
    public PsiMethod[] findSuperMethods() {
        return new PsiMethod[0];
    }

    @NotNull
    @Override
    public PsiMethod[] findSuperMethods(boolean checkAccess) {
        return new PsiMethod[0];
    }

    @NotNull
    @Override
    public PsiMethod[] findSuperMethods(PsiClass parentClass) {
        return new PsiMethod[0];
    }

    @NotNull
    @Override
    public List<MethodSignatureBackedByPsiMethod> findSuperMethodSignaturesIncludingStatic(boolean checkAccess) {
        return null;
    }

    @Override
    public PsiMethod findDeepestSuperMethod() {
        return null;
    }

    @NotNull
    @Override
    public PsiMethod[] findDeepestSuperMethods() {
        return new PsiMethod[0];
    }

    @NotNull
    @Override
    public PsiModifierList getModifierList() {
        return this;
    }

    @Override
    public boolean hasModifierProperty(@Modifier String name) {
        return false;
    }

    @Override
    public boolean hasExplicitModifier(@Modifier String name) {
        return false;
    }

    @Override
    public void setModifierProperty(@Modifier String name, boolean value) throws IncorrectOperationException {

    }

    @Override
    public void checkSetModifierProperty(@Modifier String name, boolean value) throws IncorrectOperationException {

    }

    @Nullable
    @NonNls
    public String getName() {
      return getText();
    }

    @Override
    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        return null;
    }

    @NotNull
    @Override
    public HierarchicalMethodSignature getHierarchicalMethodSignature() {
        return null;
    }

    @Override
    public PsiMethodReceiver getMethodReceiver() {
        return null;
    }

    @Override
    public PsiType getReturnTypeNoResolve() {
        return null;
    }

    @Override
    public boolean hasTypeParameters() {
        return false;
    }

    @Override
    public PsiTypeParameterList getTypeParameterList() {
        return null;
    }

    @NotNull
    @Override
    public PsiTypeParameter[] getTypeParameters() {
        return new PsiTypeParameter[0];
    }

    @Override
    public PsiDocComment getDocComment() {
        return null;
    }

    @Override
    public boolean isDeprecated() {
        return false;
    }

    @Override
    public PsiClass getContainingClass() {
        return null;
    }

    @NotNull
    @Override
    public PsiAnnotation[] getAnnotations() {
        return new PsiAnnotation[0];
    }

    @NotNull
    @Override
    public PsiAnnotation[] getApplicableAnnotations() {
        return new PsiAnnotation[0];
    }

    @Override
    public PsiAnnotation findAnnotation(@NotNull String qualifiedName) {
        return null;
    }

    @NotNull
    @Override
    public PsiAnnotation addAnnotation(@NotNull String qualifiedName) {
        return null;
    }
}
