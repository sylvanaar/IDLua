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
package com.sylvanaar.idea.Lua.editor.inspections.performance;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.editor.inspections.AbstractInspection;
import com.sylvanaar.idea.Lua.editor.inspections.utils.ControlFlowUtils;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaBinaryExpression;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

public class StringConcatenationInLoopsInspection extends AbstractInspection {

    /** @noinspection PublicField */
    public boolean m_ignoreUnlessAssigned = true;

    @Override
    @NotNull
    public String getDisplayName() {
        return "String concatenation in a loop";
    }

//    @Override
    @NotNull
    protected String buildErrorString(Object... infos) {
        return
                "String concatenation in loop";
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

//    @Override
//    public JComponent createOptionsPanel() {
//        return new SingleCheckboxOptionsPanel(
//                InspectionGadgetsBundle.message(
//                        "string.concatenation.in.loops.only.option"),
//                this, "m_ignoreUnlessAssigned");
//    }

    @Override
  public LuaElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
            return new LuaRecursiveElementVisitor() {

                @Override public void visitBinaryExpression(
                         LuaBinaryExpression expression) {
                    super.visitBinaryExpression(expression);
                    if (expression.getRightOperand() == null) {
                        return;
                    }
                    final LuaPsiElement sign = expression.getOperator();
                    final IElementType tokenType = sign.getNode().getFirstChildNode().getElementType();
                    if (!tokenType.equals(LuaTokenTypes.CONCAT)) {
                        return;
                    }
                    if (!ControlFlowUtils.isInLoop(expression)) {
                        return;
                    }
//                    if (ControlFlowUtils.isInExitStatement(expression)) {
//                        return;
//                    }
//                    if (ExpressionUtils.isEvaluatedAtCompileTime(expression)) {
//                        return;
//                    }
//                    if (containingStatementExits(expression)) {
//                        return;
//                    }
//                    if (m_ignoreUnlessAssigned && !isAppendedRepeatedly(expression)) {
//                        return;
//                    }
                    holder.registerProblem(expression, buildErrorString(), LocalQuickFix.EMPTY_ARRAY);
                }
            };
    }
    
//        private boolean containingStatementExits(PsiElement element) {
//            final PsiStatement newExpressionStatement =
//                    PsiTreeUtil.getParentOfType(element, PsiStatement.class);
//            if (newExpressionStatement == null) {
//                return containingStatementExits(element);
//            }
//            final PsiStatement parentStatement =
//                    PsiTreeUtil.getParentOfType(newExpressionStatement,
//                            PsiStatement.class);
//            return !ControlFlowUtils.statementMayCompleteNormally(
//                    parentStatement);
//        }
//
//        private boolean isAppendedRepeatedly(PsiExpression expression) {
//            PsiElement parent = expression.getParent();
//            while (parent instanceof PsiParenthesizedExpression ||
//                    parent instanceof PsiBinaryExpression) {
//                parent = parent.getParent();
//            }
//            if (!(parent instanceof PsiAssignmentExpression)) {
//                return false;
//            }
//            final PsiAssignmentExpression assignmentExpression =
//                    (PsiAssignmentExpression)parent;
//            PsiExpression lhs = assignmentExpression.getLExpression();
//            while (lhs instanceof PsiParenthesizedExpression) {
//                final PsiParenthesizedExpression parenthesizedExpression =
//                        (PsiParenthesizedExpression)lhs;
//                lhs = parenthesizedExpression.getExpression();
//            }
//            if (!(lhs instanceof PsiReferenceExpression)) {
//                return false;
//            }
//            if (assignmentExpression.getOperationTokenType() ==
//                    JavaTokenType.PLUSEQ) {
//                return true;
//            }
//            final PsiReferenceExpression referenceExpression =
//                    (PsiReferenceExpression)lhs;
//            final PsiElement element = referenceExpression.resolve();
//            if (!(element instanceof PsiVariable)) {
//                return false;
//            }
//            final PsiVariable variable = (PsiVariable)element;
//            final PsiExpression rhs = assignmentExpression.getRExpression();
//            return rhs != null &&
//                    VariableAccessUtils.variableIsUsed(variable, rhs);
//        }
    }
