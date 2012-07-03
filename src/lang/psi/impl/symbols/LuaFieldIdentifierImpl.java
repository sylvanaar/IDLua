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
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.lang.parser.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.util.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import org.jetbrains.annotations.*;

import java.lang.ref.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/15/11
 * Time: 1:31 AM
 */
public class LuaFieldIdentifierImpl extends LuaStubElementBase<LuaFieldStub> implements LuaFieldIdentifier {
    public LuaFieldIdentifierImpl(ASTNode node) {
        super(node);
    }

    public LuaFieldIdentifierImpl(LuaFieldStub stub) {
        super(stub, LuaElementTypes.FIELD_NAME);
        type = LuaStubUtils.GetStubOrPrimativeType(stub, this);
    }

    @Override
    public PsiElement getParent() {
        return getDefinitionParent();
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        LuaIdentifier node = LuaPsiElementFactoryImpl.getInstance(getProject()).createFieldNameIdentifier(name);
        replace(node);

        return this;
    }

    @Override
    public Object evaluate() {
        return getName();
    }

    private LuaType type = LuaType.ANY;

    @NotNull
    @Override
    public LuaType getLuaType() {
        if (type instanceof StubType) type = ((StubType) type).get();
        return type;
    }

    @Override
    public void setLuaType(LuaType type) {
        this.type = LuaTypeUtil.combineTypes(this.type, type);
        LuaType outerType = null;
        final LuaCompoundIdentifier compositeIdentifier = getCompositeIdentifier();

        if (compositeIdentifier != null) outerType = compositeIdentifier.getLeftSymbol().getLuaType();
        else {
            PsiElement e = getParent().getParent();
            if (e instanceof LuaTableConstructor) outerType = ((LuaTableConstructor) e).getLuaType();
        }


        if (outerType instanceof LuaTable) {
            final String name = getName();
            if (name != null) ((LuaTable) outerType).addPossibleElement(name, this.type);
        }
    }

    /**
     * Defined Value Implementation *
     */
    SoftReference<LuaExpression> definedValue = null;

    @Override
    public LuaExpression getAssignedValue() {
        return definedValue == null ? null : definedValue.get();
    }

    @Override
    public void setAssignedValue(LuaExpression value) {
        definedValue = new SoftReference<LuaExpression>(value);
    }

    /**
     * Defined Value Implementation *
     */

    @Override
    public PsiElement replaceWithExpression(LuaExpression newExpr, boolean removeUnnecessaryParentheses) {
        return LuaPsiUtils.replaceElement(this, newExpr);
    }

    @Override
    public boolean isSameKind(LuaSymbol identifier) {
        return identifier instanceof LuaFieldIdentifier;
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitIdentifier(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitIdentifier(this);
        } else {
            visitor.visitElement(this);
        }
    }

    public boolean isDeclaration() {
        return isAssignedTo();
    }

    @Override
    public boolean isAssignedTo() {
        LuaCompoundIdentifier v = getCompositeIdentifier();

        if (v == null)
            return true; // the only times fields are not part of a composite identifier are table constructors.

        return false;//v.isAssignedTo();
    }

    public LuaCompoundIdentifier getCompositeIdentifier() {
        if (getParent() instanceof LuaCompoundIdentifier) return ((LuaCompoundIdentifier) getParent());

        return null;
    }

    @Override
    public String toString() {
        return "Field: " + getText();
    }

    @Override
    public PsiReference getReference() {
        PsiElement e = getCompositeIdentifier();
        if (e == null) return null;

        return e.getReference();
    }
    //    @Override
//    public int getStartOffsetInParent() {
//        return getTextOffset()-getEnclosingIdentifier().getTextOffset();
//    }

    @Override
    public LuaCompoundIdentifier getEnclosingIdentifier() {
        LuaCompoundIdentifier v = getCompositeIdentifier();

        if (v == null)
            return null; // the only times fields are not part of a composite identifier are table constructors.
        return getCompositeIdentifier().getEnclosingIdentifier();
    }


    public PsiElement getNameIdentifier() {
        return this;
    }

    @Override
    public String getDefinedName() {
        return getName();
    }
}
