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

import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import org.jetbrains.annotations.NonNls;


public class ConditionalUtils {

  private ConditionalUtils() {
    super();
  }

  public static boolean isReturn(LuaStatementElement statement, @NonNls String value) {
    if (statement == null) {
      return false;
    }
    if (!(statement instanceof LuaReturnStatement)) {
      return false;
    }
    final LuaReturnStatement returnStatement =
        (LuaReturnStatement) statement;
    final LuaExpression returnValue = (LuaExpression) returnStatement.getReturnValue();
    if (returnValue == null) {
      return false;
    }
    final String returnValueText = returnValue.getText();
    return value.equals(returnValueText);
  }

  public static boolean isAssignment(LuaStatementElement statement, @NonNls String value) {
    if (statement == null) {
      return false;
    }
    if (!(statement instanceof LuaExpression)) {
      return false;
    }
    final LuaExpression expression = (LuaExpression) statement;
    if (!(expression instanceof LuaAssignmentStatement)) {
      return false;
    }
    final LuaAssignmentStatement assignment =
        (LuaAssignmentStatement) expression;
    final LuaExpression rhs = assignment.getRightExprs();
    if (rhs == null) {
      return false;
    }
    final String rhsText = rhs.getText();
    return value.equals(rhsText);
  }

  public static boolean isAssignment(LuaStatementElement statement) {
    return statement instanceof LuaAssignmentStatement;
  }
}
