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
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionCallStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ParenthesesUtils {

  private ParenthesesUtils() {
    super();
  }

  private static final int PARENTHESIZED_PRECEDENCE = 0;
  private static final int LITERAL_PRECEDENCE = 0;
  public static final int METHOD_CALL_PRECEDENCE = 1;

  private static final int POSTFIX_PRECEDENCE = 2;
  public static final int PREFIX_PRECEDENCE = 3;
  public static final int TYPE_CAST_PRECEDENCE = 4;
  public static final int EXPONENTIAL_PRECEDENCE = 5;
  public static final int MULTIPLICATIVE_PRECEDENCE = 6;
  private static final int ADDITIVE_PRECEDENCE = 7;
  public static final int SHIFT_PRECEDENCE = 8;
  private static final int RELATIONAL_PRECEDENCE = 9;
  public static final int EQUALITY_PRECEDENCE = 10;

  private static final int BINARY_AND_PRECEDENCE = 11;
  private static final int BINARY_XOR_PRECEDENCE = 12;
  private static final int BINARY_OR_PRECEDENCE = 13;
  public static final int AND_PRECEDENCE = 14;
  public static final int OR_PRECEDENCE = 15;
  public static final int CONDITIONAL_PRECEDENCE = 16;
  private static final int ASSIGNMENT_PRECEDENCE = 17;

  private static final int NUM_PRECEDENCES = 18;

  private static final Map<IElementType, Integer> s_binaryOperatorPrecedence =
      new HashMap<IElementType, Integer>(NUM_PRECEDENCES);

  static {
    s_binaryOperatorPrecedence.put(LuaTokenTypes.PLUS, ADDITIVE_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.MINUS, ADDITIVE_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.MULT, MULTIPLICATIVE_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.DIV, MULTIPLICATIVE_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.MOD, MULTIPLICATIVE_PRECEDENCE);

//    s_binaryOperatorPrecedence.put(LuaTokenTypes.mSTAR_STAR, EXPONENTIAL_PRECEDENCE);
//    s_binaryOperatorPrecedence.put(LuaTokenTypes.mLAND, AND_PRECEDENCE);
//    s_binaryOperatorPrecedence.put(LuaTokenTypes.mLOR, OR_PRECEDENCE);
//    s_binaryOperatorPrecedence.put(LuaTokenTypes.mBAND, BINARY_AND_PRECEDENCE);
//    s_binaryOperatorPrecedence.put(LuaTokenTypes.mBOR, BINARY_OR_PRECEDENCE);
//    s_binaryOperatorPrecedence.put(LuaTokenTypes.mBXOR, BINARY_XOR_PRECEDENCE);
   // s_binaryOperatorPrecedence.put(LuaTokenTypes.mBSL, SHIFT_PRECEDENCE);
   // s_binaryOperatorPrecedence.put(LuaTokenTypes.mBSR, SHIFT_PRECEDENCE);
  //  s_binaryOperatorPrecedence.put(">>>", SHIFT_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.GT, RELATIONAL_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.GE, RELATIONAL_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.LT, RELATIONAL_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.LE, RELATIONAL_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.EQ, EQUALITY_PRECEDENCE);
    s_binaryOperatorPrecedence.put(LuaTokenTypes.NE, EQUALITY_PRECEDENCE);
    //s_binaryOperatorPrecedence.put(LuaTokenTypes.mCOMPARE_TO, EQUALITY_PRECEDENCE);
  }

  @Nullable
  public static LuaExpression stripParentheses(
      @Nullable LuaExpression expression) {
    LuaExpression parenthesized = expression;
    while (parenthesized instanceof LuaParenthesizedExpression) {
      final LuaParenthesizedExpression parenthesizedExpression =
          (LuaParenthesizedExpression) parenthesized;
      parenthesized = parenthesizedExpression.getOperand();
    }
    return parenthesized;
  }

  public static int getPrecendence(LuaExpression expression) {
    if (
        expression instanceof LuaLiteralExpression
        ) {
      return LITERAL_PRECEDENCE;
    }
    if (expression instanceof LuaReferenceElement) {
      final LuaReferenceElement referenceExpression =
          (LuaReferenceElement) expression;
//      if (referenceExpression.getQualifierExpression() != null) {
//        return METHOD_CALL_PRECEDENCE;
//      } else {
        return LITERAL_PRECEDENCE;
//      }
    }
    if (expression instanceof LuaFunctionCallStatement) {
      return METHOD_CALL_PRECEDENCE;
    }
//    if (expression instanceof LuaTypeCastExpression ||
//        expression instanceof LuaNewExpression) {
//      return TYPE_CAST_PRECEDENCE;
//    }
//    if (expression instanceof LuaPostfixExpression) {
//      return POSTFIX_PRECEDENCE;
//    }
    if (expression instanceof LuaUnaryExpression) {
      return PREFIX_PRECEDENCE;
    }
    if (expression instanceof LuaBinaryExpression) {
      final LuaBinaryExpression binaryExpression =
          (LuaBinaryExpression) expression;
      final IElementType sign = binaryExpression.getOperationTokenType();
      if (sign != null) return precedenceForBinaryOperator(sign);
    }
    if (expression instanceof LuaConditionalExpression) {
      return CONDITIONAL_PRECEDENCE;
    }
    if (expression instanceof LuaAssignmentStatement) {
      return ASSIGNMENT_PRECEDENCE;
    }
    if (expression instanceof LuaParenthesizedExpression) {
      return PARENTHESIZED_PRECEDENCE;
    }
    return -1;
  }

  private static int precedenceForBinaryOperator(@NotNull IElementType sign) {
    return s_binaryOperatorPrecedence.get(sign);
  }

}
