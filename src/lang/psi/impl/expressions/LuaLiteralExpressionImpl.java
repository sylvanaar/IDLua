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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.lang.lexer.LuaElementType;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaLiteralExpression;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

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
        if (getLuaType() == LuaType.BOOLEAN) {
            if (getText().equals("false")) return false;
            if (getText().equals("true")) return true;
        }

        if (getLuaType() == LuaType.NUMBER) {
            try {
                return Double.parseDouble(getText());
            } catch (NumberFormatException unused) {
                return null;
            }
        }

        if (getLuaType() == LuaType.NIL) return NIL;

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


    @Override
    public LuaType getLuaType() {
        PsiElement fc = getFirstChild();
        if (fc == null) return LuaType.ANY;

        LuaElementType e = (LuaElementType) fc.getNode().getElementType();

        if (e == FALSE || e == TRUE)
            return LuaType.BOOLEAN;

        if (e == NUMBER)
            return LuaType.NUMBER;

        if (e == STRING || e == LONGSTRING)
            return LuaType.STRING;

        if (e == NIL)
            return LuaType.NIL;

        return LuaType.ANY;
    }
}