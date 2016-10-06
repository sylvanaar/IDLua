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
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.lexer.*;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaUnaryExpression;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 11:40:09 PM
 */
public class LuaUnaryExpressionImpl extends LuaExpressionImpl implements LuaUnaryExpression {
    public LuaUnaryExpressionImpl(ASTNode node) {
        super(node);
    }


    @Override
    public String toString() {
        LuaExpression expression = getExpression();
        return super.toString() + " ( " + getOperator().getText() + " " +
               (expression != null ? expression.getText() : "err") + ")";
    }

    @Override
    public LuaPsiElement getOperator() {
        return (LuaPsiElement) findChildByType(LuaElementTypes.UNARY_OP);
    }

    @Override
    public LuaExpression getExpression() {
        return findChildByClass(LuaExpression.class);
    }

    @Override
    public IElementType getOperationTokenType() {
        return getOperator().getNode().getFirstChildNode().getElementType();
    }

    @Override
    public LuaExpression getOperand() {
        return getExpression();
    }

    @NotNull
    @Override
    public LuaType getLuaType() {
        final IElementType type = getOperationTokenType();
        if (type==LuaTokenTypes.NOT) return LuaPrimitiveType.BOOLEAN;
        if (type==LuaTokenTypes.GETN) return LuaPrimitiveType.NUMBER;
        return super.getLuaType();
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitUnaryExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitUnaryExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }

}
