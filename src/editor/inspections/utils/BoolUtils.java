/*
 * Copyright 2007-2008 Dave Griffith
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
package com.sylvanaar.idea.Lua.editor.inspections.utils;

import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaConditionalExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaUnaryExpression;
import org.apache.commons.lang.ObjectUtils;
import org.jetbrains.annotations.NotNull;

public class BoolUtils {
  private static Object UNKNOWN = new Object();
  public static boolean isNegation(@NotNull LuaExpression exp) {
    if (!(exp instanceof LuaUnaryExpression)) {
      return false;
    }
    final LuaUnaryExpression prefixExp = (LuaUnaryExpression) exp;
    final IElementType sign = prefixExp.getOperationTokenType();
    return LuaTokenTypes.NOT.equals(sign);
  }

  public static boolean isTrue(LuaConditionalExpression condition) {
    Object value = ObjectUtils.defaultIfNull(condition.evaluate(), UNKNOWN);
    return value.equals(Boolean.TRUE);
  }

  public static boolean isFalse(LuaConditionalExpression condition) {
      Object value = ObjectUtils.defaultIfNull(condition.evaluate(), UNKNOWN);

      return value.equals(Boolean.FALSE);
  }

}
