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
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaBinaryExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;

import java.util.HashMap;
import java.util.Map;

public class ComparisonUtils {
  private static final Map<IElementType, String> s_comparisonStrings = new HashMap<IElementType, String>(6);
  private static final Map<IElementType, String> s_swappedComparisons = new HashMap<IElementType, String>(6);
  private static final Map<IElementType, String> s_invertedComparisons = new HashMap<IElementType, String>(6);

  private ComparisonUtils() {
    super();
  }

  static {
    s_comparisonStrings.put(LuaTokenTypes.EQ, "==");
    s_comparisonStrings.put(LuaTokenTypes.NE, "~=");
    s_comparisonStrings.put(LuaTokenTypes.GT, ">");
    s_comparisonStrings.put(LuaTokenTypes.LT, "<");
    s_comparisonStrings.put(LuaTokenTypes.GE, ">=");
    s_comparisonStrings.put(LuaTokenTypes.LE, "<=");

    s_swappedComparisons.put(LuaTokenTypes.EQ, "==");
    s_swappedComparisons.put(LuaTokenTypes.NE, "~=");
    s_swappedComparisons.put(LuaTokenTypes.GT, "<");
    s_swappedComparisons.put(LuaTokenTypes.LT, ">");
    s_swappedComparisons.put(LuaTokenTypes.GE, "<=");
    s_swappedComparisons.put(LuaTokenTypes.LE, ">=");

    s_invertedComparisons.put(LuaTokenTypes.EQ, "~=");
    s_invertedComparisons.put(LuaTokenTypes.NE, "==");
    s_invertedComparisons.put(LuaTokenTypes.GT, "<=");
    s_invertedComparisons.put(LuaTokenTypes.LT, ">=");
    s_invertedComparisons.put(LuaTokenTypes.GE, "<");
    s_invertedComparisons.put(LuaTokenTypes.LE, ">");
  }

  public static boolean isComparison(LuaExpression exp) {
    if (!(exp instanceof LuaBinaryExpression)) {
      return false;
    }
    final LuaBinaryExpression binaryExpression = (LuaBinaryExpression) exp;
    final IElementType sign = binaryExpression.getOperationTokenType();
    return s_comparisonStrings.containsKey(sign);
  }

  public static String getStringForComparison(IElementType str) {
    return s_comparisonStrings.get(str);
  }

  public static String getFlippedComparison(IElementType str) {
    return s_swappedComparisons.get(str);
  }

  public static String getNegatedComparison(IElementType str) {
    return s_invertedComparisons.get(str);
  }
}
