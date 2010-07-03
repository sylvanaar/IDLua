/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.sylvanaar.idea.Lua.intentions.utils;

import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;


public class BoolUtils {
  private BoolUtils() {
    super();
  }

  public static boolean isNegated(LuaExpression exp) {
    LuaExpression ancestor = exp;
    while (ancestor.getParent() instanceof LuaParenthesizedExpression) {
      ancestor = (LuaExpression) ancestor.getParent();
    }
    if (ancestor.getParent() instanceof LuaUnaryExpression) {
      final LuaUnaryExpression prefixAncestor =
          (LuaUnaryExpression) ancestor.getParent();
      final IElementType sign = prefixAncestor.getOperationTokenType();
      if (LuaTokenTypes.NOT.equals(sign)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public static LuaExpression findNegation(LuaExpression exp) {
    LuaExpression ancestor = exp;
    while (ancestor.getParent() instanceof LuaParenthesizedExpression) {
      ancestor = (LuaExpression) ancestor.getParent();
    }
    if (ancestor.getParent() instanceof LuaUnaryExpression) {
      final LuaUnaryExpression prefixAncestor =
          (LuaUnaryExpression) ancestor.getParent();
      final IElementType sign = prefixAncestor.getOperationTokenType();
      if (LuaTokenTypes.NOT.equals(sign)) {
        return prefixAncestor;
      }
    }
    return null;
  }

  public static boolean isNegation(LuaExpression exp) {
    if (!(exp instanceof LuaUnaryExpression)) {
      return false;
    }
    final LuaUnaryExpression prefixExp = (LuaUnaryExpression) exp;
    final IElementType sign = prefixExp.getOperationTokenType();
    return LuaTokenTypes.NOT.equals(sign);
  }

  public static LuaExpression getNegated(LuaExpression exp) {
    final LuaUnaryExpression prefixExp = (LuaUnaryExpression) exp;
    final LuaExpression operand = prefixExp.getOperand();
    return ParenthesesUtils.stripParentheses(operand);
  }

  public static boolean isBooleanLiteral(LuaExpression exp) {
    if (exp instanceof LuaLiteralExpression) {
      final LuaLiteralExpression expression = (LuaLiteralExpression) exp;
      @NonNls final String text = expression.getText();
      return LuaTokenTypes.TRUE.equals(text) || LuaTokenTypes.FALSE.equals(text);
    }
    return false;
  }

  public static String getNegatedExpressionText(LuaExpression condition) {
    if (isNegation(condition)) {
      final LuaExpression negated = getNegated(condition);
      return negated.getText();
    } else if (ComparisonUtils.isComparison(condition)) {
      final LuaBinaryExpression binaryExpression =
          (LuaBinaryExpression) condition;
      final IElementType sign = binaryExpression.getOperationTokenType();
      final String negatedComparison =
          ComparisonUtils.getNegatedComparison(sign);
      final LuaExpression lhs = binaryExpression.getLeftOperand();
      final LuaExpression rhs = binaryExpression.getRightOperand();
      assert rhs != null;
      return lhs.getText() + negatedComparison + rhs.getText();
    } else if (ParenthesesUtils.getPrecendence(condition) >
        ParenthesesUtils.PREFIX_PRECEDENCE) {
      return "not (" + condition.getText() + ')';
    } else {
      return "not " + condition.getText();
    }
  }
}

