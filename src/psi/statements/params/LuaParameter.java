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

package com.sylvanaar.idea.Lua.psi.statements.params;

import com.intellij.psi.PsiParameter;
import com.sylvanaar.idea.Lua.psi.statements.LuaVariable;
import com.sylvanaar.idea.Lua.psi.statements.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.psi.types.LuaTypeElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Dmitry.Krasilschikov
 * @date: 26.03.2007
 */
public interface LuaParameter extends PsiParameter, LuaVariable {
  public static final LuaParameter[] EMPTY_ARRAY = new LuaParameter[0];

  @Nullable
  LuaTypeElement getTypeElementLua();

  @Nullable
  LuaExpression getDefaultInitializer();

  boolean isOptional();
}
