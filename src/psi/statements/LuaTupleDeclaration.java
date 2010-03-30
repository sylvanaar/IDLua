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

package com.sylvanaar.idea.Lua.psi.statements;

import com.sylvanaar.idea.Lua.psi.LuaElement;
import com.sylvanaar.idea.Lua.psi.statements.expressions.LuaExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



/**
 * Created by IntelliJ IDEA.
 * User: Dmitry.Krasilschikov
 * Date: 16.02.2009
 * Time: 17:26:49
 * To change this template use File | Settings | File Templates.
 */
public interface LuaTupleDeclaration extends LuaElement {
  @NotNull
 LuaVariable[] getVariables();

  @Nullable
  LuaExpression getInitializerGroovy();

  int getVariableNumber(@NotNull LuaVariable variable);
}
