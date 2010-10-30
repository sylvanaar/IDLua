/*
 * Copyright 2007-2008 Dave Griffith
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
package com.sylvanaar.idea.Lua.editor.inspections.bugs;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.editor.inspections.AbstractInspection;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaBinaryExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class LuaDivideByZeroInspection extends AbstractInspection {

    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return PROBABLE_BUGS;
    }

    @Nls
    @NotNull
    public String getDisplayName() {
        return "Divide by zero";
    }

    @Override
    public String getStaticDescription() {
        return "Looks for division by 0";
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new LuaElementVisitor() {
            @Override
            public void visitBinaryExpression(LuaBinaryExpression expression) {
                super.visitBinaryExpression(expression);
                final LuaExpression rhs = expression.getRightOperand();
                if (rhs == null) {
                    return;
                }
                final IElementType tokenType = expression.getOperationTokenType();
                if (!LuaTokenTypes.DIV.equals(tokenType) &&
                        !LuaTokenTypes.MOD.equals(tokenType)) {
                    return;
                }
                if (!isZero(rhs)) {
                    return;
                }
                holder.registerProblem(expression, "Divide by zero");
            }
        };
    }


    private static boolean isZero(LuaExpression expression) {
        @NonNls
        final String text = expression.getText();
        return "0".equals(text) ||
                "0x0".equals(text) ||
                "0X0".equals(text) ||
                "0.0".equals(text) ||
                "0L".equals(text) ||
                "0l".equals(text);
    }
}