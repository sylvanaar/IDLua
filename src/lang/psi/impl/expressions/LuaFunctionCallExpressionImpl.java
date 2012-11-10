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

import com.intellij.lang.*;
import com.intellij.psi.*;
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.lists.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Aug 28, 2010
 * Time: 10:04:11 AM
 */
public class LuaFunctionCallExpressionImpl extends LuaExpressionImpl implements LuaFunctionCallExpression, PsiNamedElement {
    public LuaFunctionCallExpressionImpl(ASTNode node) {
        super(node);
    }

    public String toString() {
        return "Call: " + getNameRaw();
    }

            @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitFunctionCall(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitFunctionCall(this);
        } else {
            visitor.visitElement(this);
        }
    }

    public String getNameRaw() {
        LuaReferenceElement e = findChildByClass(LuaReferenceElement.class);

        if (e != null) return e.getText();

        return null;
    }

    @Override
    public String getName() {
        LuaReferenceElement e = findChildByClass(LuaReferenceElement.class);

        if (e != null) return e.getName();

        return null;
    }

    LuaFunctionCallExpression guard;
    @NotNull
    @Override
    public LuaType getLuaType() {
        if (guard == this) return LuaPrimitiveType.ANY;

        guard = this;
        LuaType retType;
        final PsiElement firstChild = getFirstChild();
        if (firstChild instanceof LuaExpression) {
            LuaExpression e = (LuaExpression) firstChild;
            retType = e.getLuaType();
        } else retType = super.getLuaType();
        guard = null;

        if (retType instanceof LuaTypeSet) {
            final Iterator<LuaType> iterator = ((LuaTypeSet) retType).getTypeSet().iterator();
            LuaType returns = LuaPrimitiveType.ANY;
            while (iterator.hasNext()) {
                LuaType type = iterator.next();
                if (type instanceof LuaFunction)
                    returns = LuaTypeUtil.combineTypes(returns, ((LuaFunction) type).getReturnType());
            }

            return returns;
        } else if (retType instanceof LuaFunction)
            return ((LuaFunction) retType).getReturnType();

        return LuaPrimitiveType.ANY;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        return null;
    }

    @Override
    @Nullable
    public LuaExpressionList getArgumentList() {
        final LuaFunctionArguments arguments = findChildByClass(LuaFunctionArguments.class);
        if (arguments != null) {
            return arguments.getExpressions();
        }
        return null;
    }

    @Override
    public LuaReferenceElement getFunctionNameElement() {
        return findChildByClass(LuaReferenceElement.class);
    }
}
