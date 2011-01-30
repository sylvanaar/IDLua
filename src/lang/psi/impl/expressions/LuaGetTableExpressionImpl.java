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
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaGetTableExpression;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/20/11
 * Time: 3:44 AM
 */
public class LuaGetTableExpressionImpl extends LuaExpressionImpl implements LuaGetTableExpression {
    private String operator;

    public LuaGetTableExpressionImpl(ASTNode node) {
        super(node);
    }


    @Nullable
    public LuaExpression getRightSymbol() {
        LuaExpression[] e = findChildrenByClass(LuaExpression.class);
        return e.length>1?e[1]:null;
    }

    @Nullable
    public LuaExpression getLeftSymbol() {
        LuaExpression[] e = findChildrenByClass(LuaExpression.class);
        return e.length>0?e[0]:null;
    }

    @Nullable
    @Override
    public String toString() {
        return "GetTable: " +  getLeftSymbol() + getOperator() + getRightSymbol();
    }

    public String getOperator() {
        return findChildByType(LuaElementTypes.DOT).getText();
    }
}
