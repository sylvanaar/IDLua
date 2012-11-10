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
import com.sylvanaar.idea.Lua.lang.lexer.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import org.jetbrains.annotations.*;

import static com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 13, 2010
 * Time: 12:11:12 AM
 */
public class LuaLiteralExpressionImpl extends LuaExpressionImpl implements LuaLiteralExpression {
    public LuaLiteralExpressionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "Literal:" + getText();
    }

    @Override
    public Object evaluate() {
        return getValue();
    }

    @Override
    public Object getValue() {
        if (getLuaType() == LuaPrimitiveType.BOOLEAN) {
            if (getText().equals("false")) return Boolean.FALSE;
            if (getText().equals("true")) return Boolean.TRUE;
        }

        if (getLuaType() == LuaPrimitiveType.NUMBER) {
            try {
                return Double.parseDouble(getText());
            } catch (NumberFormatException unused) {
                return null;
            }
        }

        if (getLuaType() == LuaPrimitiveType.NIL) return NIL_VALUE;

        return null;
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitLiteralExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitLiteralExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }


    @NotNull
    @Override
    public LuaType getLuaType() {
        PsiElement fc = getFirstChild();
        if (fc == null) return LuaPrimitiveType.ANY;

        LuaElementType e = (LuaElementType) fc.getNode().getElementType();

        if (e == FALSE || e == TRUE)
            return LuaPrimitiveType.BOOLEAN;

        if (e == NUMBER)
            return LuaPrimitiveType.NUMBER;

        if (e == STRING || e == LONGSTRING)
            return LuaPrimitiveType.STRING;

        if (e == NIL)
            return LuaPrimitiveType.NIL;

        return LuaPrimitiveType.ANY;
    }


}