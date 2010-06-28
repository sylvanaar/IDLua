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
//import com.sylvanaar.idea.Lua.intentions.utils.BoolUtils;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaConditionalExpression;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
//import org.jetbrains.annotations.NotNull;
//
//
//public class FlipConditionalIntention extends Intention {
//
//
//  @NotNull
//  public PsiElementPredicate getElementPredicate() {
//    return new ConditionalPredicate();
//  }
//
//  public void processIntention(@NotNull PsiElement element)
//      throws IncorrectOperationException {
//    final LuaConditionalExpression exp =
//        (LuaConditionalExpression) element;
//
//    final LuaExpression condition = exp.getCondition();
//    final LuaExpression elseExpression = exp.getElseBranch();
//    final LuaExpression thenExpression = exp.getThenBranch();
//    assert elseExpression != null;
//    assert thenExpression != null;
//    final String newExpression =
//        BoolUtils.getNegatedExpressionText(condition) + '?' +
//            elseExpression.getText() +
//            ':' +
//            thenExpression.getText();
//    IntentionUtils.replaceExpression(newExpression, exp);
//  }
//
//}
