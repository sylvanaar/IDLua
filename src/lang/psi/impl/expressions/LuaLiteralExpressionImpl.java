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
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaLiteralExpression;

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
        return super.toString() + " (" + getText() + ")";
    }

    @Override
    public Object getValue() {
        return getNode().getText();
    }
}