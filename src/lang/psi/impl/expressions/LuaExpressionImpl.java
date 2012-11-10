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
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaPrimitiveType;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 11:38:37 PM
 */
public class LuaExpressionImpl extends LuaPsiElementImpl implements LuaExpression {
    private LuaType type = LuaPrimitiveType.ANY;

    public LuaExpressionImpl(ASTNode node) {
        super(node);
    }

    public String toString() {
        return getExpressionLabel() + ": " + getText().substring(0, Math.min(getText().length(), 10));
    }

    protected String getExpressionLabel() {
        return "Expr";
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newExpr, boolean removeUnnecessaryParentheses) {
        return LuaPsiUtils.replaceElement(this, newExpr);
    }

    @NotNull @Override
    public LuaType getLuaType() {
        return this.type;
    }

    @Override
    public void setLuaType(LuaType type) {
        this.type = type;
    }

    @Override
    public Object evaluate() {
        return null;
    }
}
