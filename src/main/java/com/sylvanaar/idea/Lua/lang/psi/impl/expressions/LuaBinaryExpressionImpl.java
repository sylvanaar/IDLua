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
import com.intellij.psi.tree.*;
import com.sylvanaar.idea.Lua.lang.lexer.*;
import com.sylvanaar.idea.Lua.lang.parser.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import org.jetbrains.annotations.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 11:37:52 PM
 */
public class LuaBinaryExpressionImpl extends LuaExpressionImpl implements LuaBinaryExpression {
    public LuaBinaryExpressionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public LuaPsiElement getOperator() {
        return (LuaPsiElement) findChildByType(LuaElementTypes.BINARY_OP);
    }

    @Override
    public String toString() {
        try {
        return super.toString() + " ("  + getLeftExpression().getText() + ") " + getOperator().getText() + " (" + getRightExpression().getText() +  ")";
        } catch (Throwable ignored) {}

        return "err";
    }

    @Override
    public LuaExpression getLeftExpression() {
        LuaExpression[] e = findChildrenByClass(LuaExpression.class);
        return  e.length>0?e[0]:null;
    }

    @Override
    public IElementType getOperationTokenType() {
        return getOperator().getNode().getFirstChildNode().getElementType();
    }

    @NotNull
    @Override
    public LuaType getLuaType() {
        final IElementType type = getOperationTokenType();
        if (type == LuaTokenTypes.CONCAT)
            return LuaPrimitiveType.STRING;
        if (LuaTokenTypes.COMPARE_OPS.contains(type))
            return LuaPrimitiveType.BOOLEAN;
        if (LuaTokenTypes.ARITHMETIC_OPS.contains(type))
            return LuaPrimitiveType.NUMBER;
        return super.getLuaType();
    }

    //
//    @Override
//    public Object evaluate() {
//        final LuaExpression leftExpression = getLeftExpression();
//        Object left = leftExpression.evaluate();
//        if (left == null) return null;
//        LuaType leftType = leftExpression.getLuaType();
//
//        final LuaExpression rightExpression = getRightExpression();
//        Object right = rightExpression.evaluate();
//        if (right == null) return null;
//        LuaType rightType = rightExpression.getLuaType();
//
//        final IElementType op = getOperationTokenType();
//        if (op == CONCAT && leftType == LuaType.STRING && (rightType == LuaType.STRING || rightType == LuaType.NUMBER))
//            return left.toString() + right.toString();
//
////        if (op == PLUS)
////            return Double.valueOf(left) + Double.valueOf(right);
//
//        return null;
//    }

    @Override
    public LuaExpression getLeftOperand() {
        return getLeftExpression();
    }

    @Override
    public LuaExpression getRightOperand() {
        return getRightExpression();
    }

    @Override
    public LuaExpression getRightExpression() {
        LuaExpression[] e = findChildrenByClass(LuaExpression.class);
        return  e.length>1?e[1]:null;
    }
        @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitBinaryExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitBinaryExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }
}
