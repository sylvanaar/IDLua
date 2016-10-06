///*
// * Copyright 2012 Jon S Akhtar (Sylvanaar)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package com.sylvanaar.idea.Lua.lang.psi.util;
//
//import com.intellij.psi.PsiConstantEvaluationHelper;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.impl.ConstantExpressionEvaluator;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaLiteralExpression;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParenthesizedExpression;
//import org.jetbrains.annotations.Nullable;
//
///**
// * Created by IntelliJ IDEA.
// * User: Jon S Akhtar
// * Date: 2/17/12
// * Time: 8:47 AM
// */
//public class LuaConstantExpressionEvaluator implements ConstantExpressionEvaluator{
//    @Nullable
//    public static Object evaluate(@Nullable LuaExpression expression) {
//      if (expression instanceof LuaParenthesizedExpression) {
//        return evaluate(((LuaParenthesizedExpression)expression).getOperand());
//      }
//
//      if (expression instanceof LuaLiteralExpression) {
//        return ((LuaLiteralExpression) expression).getValue();
//      }
//      return null;
//    }
//
//    @Nullable
//    public Object computeConstantExpression(PsiElement expression, boolean throwExceptionOnOverflow) {
//      if (!(expression instanceof LuaExpression)) return null;
//      return evaluate((LuaExpression) expression);
//    }
//
//    @Nullable
//    public Object computeExpression(PsiElement expression,
//                                    boolean throwExceptionOnOverflow,
//                                    @Nullable PsiConstantEvaluationHelper.AuxEvaluator auxEvaluator) {
//      if (!(expression instanceof LuaExpression)) return null;
//      return evaluate((LuaExpression) expression);
//    }
//}
