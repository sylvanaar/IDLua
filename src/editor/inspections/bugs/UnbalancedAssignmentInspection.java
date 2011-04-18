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
import com.sylvanaar.idea.Lua.editor.inspections.utils.ExpressionUtils;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElementFactory;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFunctionCallExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDeclarationStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaLocalDefinitionStatement;
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
        return "Looks for unbalanced assignment statements where the number of identifiers on the left could be " +
               "different than the number of expressions on the right.";
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
                checkAssignment(assign, left, right, holder);
            }

            @Override
            public void visitDeclarationStatement(LuaDeclarationStatement e) {
                super.visitDeclarationStatement(e);

                if (e instanceof LuaLocalDefinitionStatement) {
                    LuaIdentifierList left = ((LuaLocalDefinitionStatement) e).getLeftExprs();
                    LuaExpressionList right = ((LuaLocalDefinitionStatement) e).getRightExprs();

                    if (right == null)
                        return;

                    if (ExpressionUtils.onlyNilExpressions(right))
                        return;

                    if (right.count() > 0)
                        checkAssignment(e, left, right, holder);
                }
            }
        };
    }

    private void checkAssignment(PsiElement element,
                                 LuaIdentifierList left,
                                 LuaExpressionList right,
                                 ProblemsHolder holder) {
        if (left != null && right != null && left.count() != right.count()) {

            boolean tooManyExprs = left.count() < right.count();
            boolean ignore = false;

            int exprcount = right.getLuaExpressions().size();
            ignore = exprcount == 0;

            PsiElement expr = null;

            if (!ignore) {
                LuaExpression last = right.getLuaExpressions().get(exprcount - 1);

                expr = last;

                if (expr instanceof LuaCompoundIdentifier)
                    expr = ((LuaCompoundIdentifier) expr).getScopeIdentifier();
            }

            if (expr != null)
                ignore = (expr.getText()).equals("...");
            else
                ignore = true;

            if (!ignore && expr instanceof LuaFunctionCallExpression)
                ignore = true;

            if (!ignore) {
                LocalQuickFix[] fixes = {new UnbalancedAssignmentFix(tooManyExprs)};
                holder.registerProblem(element, "Unbalanced number of expressions in assignment", fixes);
            }
        }
    }


    private class UnbalancedAssignmentFix extends LuaFix {
        boolean tooManyExprs;

        public UnbalancedAssignmentFix(boolean tooManyExprs) {
            this.tooManyExprs = tooManyExprs;
        }


        @Override
        protected void doFix(Project project, ProblemDescriptor descriptor) throws IncorrectOperationException {
            final LuaAssignmentStatement assign = (LuaAssignmentStatement) descriptor.getPsiElement();
            final LuaIdentifierList identifierList = assign.getLeftExprs();
            final LuaExpressionList expressionList = assign.getRightExprs();
            final PsiElement lastExpr = expressionList.getLastChild();
            final int leftCount = identifierList.count();
            final int rightCount = expressionList.count();

            if (tooManyExprs) {
                for (int i = rightCount - leftCount; i > 0; i--) {
                    identifierList.addAfter(LuaPsiElementFactory.getInstance(project).createExpressionFromText("_"), lastExpr);
                }
            } else {
                for (int i = leftCount - rightCount; i > 0; i--) {
                    expressionList.addAfter(LuaPsiElementFactory.getInstance(project).createExpressionFromText("nil"), lastExpr);
                }
            }
        }

        @NotNull
        @Override
        public String getName() {
            if (tooManyExprs)
                return "Balance by adding '_' identifiers on the left";
            else
                return "Balance by adding nil's on the right";
        }
    }

}
