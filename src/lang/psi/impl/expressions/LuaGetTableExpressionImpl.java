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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaGetTableExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaVariable;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/20/11
 * Time: 3:44 AM
 */
public class LuaGetTableExpressionImpl extends LuaVariableImpl implements LuaGetTableExpression {
    public LuaGetTableExpressionImpl(ASTNode node) {
        super(node);
    }


    public LuaSymbol getRightSymbol() {
        LuaSymbol[] e = findChildrenByClass(LuaSymbol.class);
        if (e.length < 2)
            return null;

        return e[1];
    }

    public LuaSymbol getLeftSymbol() {
        return findChildrenByClass(LuaSymbol.class)[0];
    }

    @Override
    public PsiElement getElement() {
        return getRightSymbol();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String toString() {
        try {
            return "GetTable: " + getLeftSymbol().getText() + " -> " + getRightSymbol().getText();
        } catch (Throwable unused) {
        }

        return "err";
    }

    @Override
    public LuaNamedElement getPrimaryIdentifier() {
        LuaExpression e = getLeftSymbol();
        if (e instanceof LuaVariable)
            return (LuaNamedElement) e;

        if (e instanceof LuaReferenceElement) {
            PsiElement pe = ((LuaReferenceElement) e).getElement();

            if (pe instanceof LuaNamedElement)
                return (LuaNamedElement) pe;
        }

        return null;
    }


    public TextRange getRangeInElement() {
        final LuaExpression nameElement = getRightSymbol();
        return new TextRange(0, nameElement != null ? nameElement.getTextLength() : 0);
    }

}
