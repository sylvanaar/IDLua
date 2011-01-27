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

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaGetTableExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaReferenceExpressionImpl;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/20/11
 * Time: 3:44 AM
 */
public class LuaGetTableExpressionImpl extends LuaReferenceExpressionImpl implements LuaGetTableExpression {
    public LuaGetTableExpressionImpl(ASTNode node) {
            super(node);
    }

    @Override
    public LuaPsiElement getOperator() {
        return (LuaPsiElement) findChildByType(LuaElementTypes.BINARY_OP);
    }

    @Override
    public LuaExpression getRightExpression() {
       LuaExpression[] e = findChildrenByClass(LuaExpression.class);
       if (e == null || e.length < 2)
           return null;

       return e[1];
    }

    @Override
    public ASTNode getNameElement() {
        LuaExpression e = getRightExpression();
        return e!=null?e.getNode():null;
    }

    @Override
    public PsiElement getElement() {
        return getRightExpression();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public IElementType getOperationTokenType() {
        return LuaTokenTypes.DOT;
    }

    @Override
    public LuaExpression getLeftOperand() {
        return getLeftExpression();
    }

    @Override
    public LuaExpression getRightOperand() {
        return getRightExpression();
    }

    @Override
    public LuaExpression getLeftExpression() {
        return findChildrenByClass(LuaExpression.class)[0];
    }



    @Override
    public String toString() {
        try {
        return "GetTable: " + getLeftExpression().getText() + " -> " + getRightExpression().getText();
        } catch (Throwable unused) {}

        return "err";
    }

}
