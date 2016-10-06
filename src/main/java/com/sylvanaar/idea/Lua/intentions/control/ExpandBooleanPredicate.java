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
//import com.intellij.openapi.diagnostic.Logger;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.PsiType;
//import com.sylvanaar.idea.Lua.intentions.base.PsiElementPredicate;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaLiteralExpression;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
//
//
//class ExpandBooleanPredicate implements PsiElementPredicate {
//  private static final Logger LOGGER = Logger.getInstance("ExpandBooleanPredicate");
//
//  public boolean satisfiedBy(PsiElement element) {
//    if (!(element instanceof LuaStatementElement)) {
//      return false;
//    }
//    final LuaStatementElement statement = (LuaStatementElement) element;
//    return isBooleanReturn(statement) || isBooleanAssignment(statement);
//  }
//
//  public static boolean isBooleanReturn(LuaStatementElement statement) {
//    if (!(statement instanceof LuaReturnStatement)) {
//      return false;
//    }
//    final LuaReturnStatement returnStatement =
//        (LuaReturnStatement) statement;
//    final LuaExpression returnValue = (LuaExpression) returnStatement.getReturnValue();
//    if (returnValue == null) {
//      return false;
//    }
//    if (returnValue instanceof LuaLiteralExpression) {
//      return false;
//    }
//    final PsiType returnType = returnValue.getType();
//    if (returnType == null) {
//      return false;
//    }
//    return returnType.equals(PsiType.BOOLEAN);
//  }
//
//  public static boolean isBooleanAssignment(LuaStatementElement expression) {
//
//    if (!(expression instanceof LuaAssignmentStatement)) {
//      return false;
//    }
//    final LuaAssignmentStatement assignment =
//        (LuaAssignmentStatement) expression;
//    final LuaExpression rhs = assignment.getRightExprs();
//    if (rhs == null) {
//      return false;
//    }
//    if (rhs instanceof LuaLiteralExpression) {
//      return false;
//    }
//    final PsiType assignmentType = rhs.getType();
//    if (assignmentType == null) {
//      return false;
//    }
//    return assignmentType.equals(PsiType.BOOLEAN);
//  }
//}
