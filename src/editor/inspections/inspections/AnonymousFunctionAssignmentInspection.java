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

package com.sylvanaar.idea.Lua.editor.inspections.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 13, 2010
 * Time: 7:10:29 AM
 */
public class AnonymousFunctionAssignmentInspection extends AbstractInspection {
    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Anonymous Function Assignment";
    }

    @NotNull
    @Override
    public String getGroupDisplayName() {
        return PROBABLE_BUGS;
    }

    @Override
    public String getStaticDescription() {
        return "Looks for a = function() end and suggests provides a fix to function a() end.";
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new LuaElementVisitor() {
            public void visitAssignment(LuaAssignmentStatement assign) {
                super.visitAssignment(assign);
                if (assign.getLeftExprs().count()==1 && assign.getRightExprs().count()==1) {
                    LuaExpressionList rhs = assign.getRightExprs();
                    LuaIdentifierList lhs = assign.getLeftExprs();

                    LuaReferenceExpression name = lhs.getReferenceExprs()[0];
                    LuaExpression last = rhs.getLuaExpressions().get(0);

                    PsiElement expr = last.getLastChild();
                    boolean ignore = true;

                    if (expr instanceof LuaAnonymousFunctionExpression)
                        ignore = false;

                    if (!ignore)
                        holder.registerProblem(assign, "simple assignment of anonymous function", LocalQuickFix.EMPTY_ARRAY);
                }
            }
        };
    }


}
