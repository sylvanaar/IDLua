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

import com.intellij.codeHighlighting.*;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.*;
import com.intellij.psi.*;
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.editor.inspections.*;
import com.sylvanaar.idea.Lua.editor.inspections.utils.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.lists.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import org.jetbrains.annotations.*;

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
                if (assign instanceof LuaLocalDefinitionStatement) {
                    LuaIdentifierList left = assign.getLeftExprs();
                    LuaExpressionList right = assign.getRightExprs();

                    if (right == null || right.count() == 0) return;

                    if (ExpressionUtils.onlyNilExpressions(right))
                        return;

                    if (right.count() > 0)
                        checkAssignment(assign, left, right, holder);
                } else {
                    LuaIdentifierList left = assign.getLeftExprs();
                    LuaExpressionList right = assign.getRightExprs();
                    checkAssignment(assign, left, right, holder);
                }

            }

            @Override
            public void visitDeclarationStatement(LuaDeclarationStatement e) {
                super.visitDeclarationStatement(e);


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
                    LuaExpression extraExpression = LuaPsiElementFactory.getInstance(project).createExpressionFromText("_");
                    assert extraExpression != null : "Failed to create extra expression";
                    identifierList.addAfter(extraExpression, lastExpr);
                }
            } else {
                for (int i = leftCount - rightCount; i > 0; i--) {
                    LuaExpression nilExpression = LuaPsiElementFactory.getInstance(project).createExpressionFromText("nil");
                    assert nilExpression != null : "Failed to create nil expression";
                    expressionList.addAfter(nilExpression, lastExpr);
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
