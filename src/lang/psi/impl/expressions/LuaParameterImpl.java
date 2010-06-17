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
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;



public class LuaParameterImpl extends LuaIdentifierImpl implements LuaPsiElement, PsiParameter {
  public LuaParameterImpl(@NotNull ASTNode node) {
    super(node);
  }


  public String toString() {
    return "Parameter ("+getText()+")";
  }



    @NotNull
    @Override
    public PsiElement getDeclarationScope() {
        return getContext();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isVarArgs() {
        return (getNode().getElementType() == LuaElementTypes.ELLIPSIS);        
    }

    @NotNull
    @Override
    public PsiAnnotation[] getAnnotations() {
        return new PsiAnnotation[0];  
    }

    @NotNull
    @Override
    public PsiType getType() {
        return PsiType.VOID;  
    }

    @NotNull
    @Override
    public PsiTypeElement getTypeElement() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiExpression getInitializer() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasInitializer() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void normalizeDeclaration() throws IncorrectOperationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object computeConstantValue() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiIdentifier getNameIdentifier() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement setName(@NotNull @NonNls String s) throws IncorrectOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiType getTypeNoResolve() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiModifierList getModifierList() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasModifierProperty(@NotNull @Modifier String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
