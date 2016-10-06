///*
// * Copyright 2000-2009 JetBrains s.r.o.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.sylvanaar.idea.Lua.intentions.control;
//
//import com.intellij.psi.PsiElement;
//import com.intellij.util.IncorrectOperationException;
//import com.sylvanaar.idea.Lua.intentions.base.Intention;
//import com.sylvanaar.idea.Lua.intentions.base.IntentionUtils;
//import com.sylvanaar.idea.Lua.intentions.base.PsiElementPredicate;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
//import com.sylvanaar.idea.Lua.lang.psi.lists.LuaIdentifierList;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
//import org.jetbrains.annotations.NonNls;
//import org.jetbrains.annotations.NotNull;
//
//
//public class ExpandBooleanIntention extends Intention {
//
//
//  @NotNull
//  public PsiElementPredicate getElementPredicate() {
//    return new ExpandBooleanPredicate();
//  }
//
//  public void processIntention(@NotNull PsiElement element)
//      throws IncorrectOperationException {
//    final LuaStatementElement containingStatement = (LuaStatementElement) element;
//    if (ExpandBooleanPredicate.isBooleanAssignment(containingStatement)) {
//
//      final LuaAssignmentStatement assignmentExpression =
//          (LuaAssignmentStatement) containingStatement;
//      final LuaExpression rhs = assignmentExpression.getRightExprs();
//      assert rhs != null;
//      final String rhsText = rhs.getText();
//      final LuaIdentifierList lhs = assignmentExpression.getLeftExprs();
//      final String lhsText = lhs.getText();
//      @NonNls final String statement =
//          "if " + rhsText + " then " + lhsText + " = true else " +
//              lhsText +
//              " = false end";
//      IntentionUtils.replaceStatement(statement, containingStatement);
//    } else if (ExpandBooleanPredicate.isBooleanReturn(containingStatement)) {
//      final LuaReturnStatement returnStatement =
//          (LuaReturnStatement) containingStatement;
//      final LuaExpression returnValue = (LuaExpression) returnStatement.getReturnValue();
//      final String valueText = returnValue.getText();
//      @NonNls final String statement =
//          "if " + valueText + " then return true else return false end";
//      IntentionUtils.replaceStatement(statement, containingStatement);
//    }
//  }
//}
