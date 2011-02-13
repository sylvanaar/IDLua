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

package com.sylvanaar.idea.Lua.editor.inspections.bugs;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.editor.inspections.AbstractInspection;
import com.sylvanaar.idea.Lua.editor.inspections.LuaFix;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElementFactory;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFunctionCallExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 13, 2010
 * Time: 7:10:29 AM
 */
public class UnbalancedAssignmentInspection extends AbstractInspection {
    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Unbalanced Assignment";
    }

    @NotNull
    @Override
    public String getGroupDisplayName() {
        return PROBABLE_BUGS;
    }

    @Override
    public String getStaticDescription() {
        return "Looks for uneven assignment expressions where number of parameters being assigned to could be less than the number of assignments.";
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
                LuaIdentifierList left = assign.getLeftExprs();
                LuaExpressionList right = assign.getRightExprs();
                if (left !=null && right != null &&
                        assign.getLeftExprs().count() > assign.getRightExprs().count()) {
                    LuaExpressionList rhs = assign.getRightExprs();

                    if (rhs == null)
                        return;

                    boolean ignore = false;

                    int exprcount = rhs.getLuaExpressions().size();
                    ignore = exprcount == 0;

                    PsiElement expr = null;
                    
                    if (!ignore) {
                        LuaExpression last = rhs.getLuaExpressions().get(exprcount-1);

                         expr = last;

                        if (expr instanceof LuaCompoundIdentifier)
                            expr = ((LuaCompoundIdentifier) expr).getScopeIdentifier();
                    }

                    if (expr != null)
                        ignore = (expr.getText()).equals("...") ;
                    else
                        ignore = true;

                    if (!ignore && expr instanceof LuaFunctionCallExpression)
                        ignore = true;

                    if (!ignore) {
                        LocalQuickFix[] fixes = { new UnbalancedAssignmentFix() };
                        holder.registerProblem(assign, "Unbalanced number of expressions in assignment", fixes);
                    }
                }
            }
        };
    }


    private class UnbalancedAssignmentFix extends LuaFix {

        @Override
        protected void doFix(Project project, ProblemDescriptor descriptor) throws IncorrectOperationException {
            final LuaAssignmentStatement assign = (LuaAssignmentStatement) descriptor.getPsiElement();
            final LuaExpressionList expressionList = assign.getRightExprs();
            final PsiElement lastExpr = expressionList.getLastChild();
            final int leftCount = assign.getLeftExprs().count();
            final int rightCount = expressionList.count();

            assert leftCount > rightCount;

            for(int i = leftCount - rightCount; i > 0; i--)
                expressionList.addAfter(LuaPsiElementFactory.getInstance(project).createExpressionFromText("nil"),
                        lastExpr);
        }

        @NotNull
        @Override
        public String getName() {
            return "Pad with nil expressions";
        }
    }

}
