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
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaConditionalExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaPrimitiveType;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 28, 2010
 * Time: 4:38:10 AM
 */
public class LuaConditionalExpressionImpl extends LuaExpressionImpl implements LuaConditionalExpression {
    public LuaConditionalExpressionImpl(ASTNode node) {
        super(node);
    }

    public String toString() {
        return "Conditional: " + super.toString();
    }

    @Override
    public LuaExpression getOperand() {
        final PsiElement element = getFirstChild();

        return element instanceof LuaExpression ? (LuaExpression) element : null;
    }

    @NotNull
    @Override
    public LuaType getLuaType() {
        return LuaPrimitiveType.BOOLEAN;
    }

    @Override
    public Object evaluate() {
        Object value = getOperand().evaluate();

        if (value == null)
            return value;

        if (value == Boolean.FALSE || value == NIL_VALUE)
            return Boolean.FALSE;

        return Boolean.TRUE;
    }
}
