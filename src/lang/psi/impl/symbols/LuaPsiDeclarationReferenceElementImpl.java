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

import com.intellij.lang.*;
import com.intellij.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.util.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import org.jetbrains.annotations.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/28/11
 * Time: 10:13 PM
 */
public abstract class LuaPsiDeclarationReferenceElementImpl extends LuaReferenceElementImpl
        implements LuaDeclarationExpression, LuaReferenceElement {
    public LuaPsiDeclarationReferenceElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newExpr, boolean removeUnnecessaryParentheses) {
        return LuaPsiUtils.replaceElement(this, newExpr);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
//        visitor.visitReferenceElement(this);
        visitor.visitDeclarationExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
//            ((LuaElementVisitor) visitor).visitReferenceElement(this);
            ((LuaElementVisitor) visitor).visitDeclarationExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public PsiReference getReference() {
        return this;
    }

    @Override
    public PsiElement getElement() {
        return this;
    }

    @Override
    public boolean checkSelfReference(PsiElement element) {
        return element == this;
    }

    @Override
    public boolean isAssignedTo() {
        return true;
    }

    @Override
    public String getName() {
        return getText();
    }

    protected LuaType type = LuaPrimitiveType.ANY;

    @NotNull
    @Override
    public LuaType getLuaType() {
        return type;
    }

    @Override
    public void setLuaType(LuaType type) {
        this.type = LuaTypeUtil.combineTypes(this.type, type);
    }
}



