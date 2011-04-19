/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.controlFlow.impl;

import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;

/**
* @author peter
*/
public class MaybeReturnInstruction extends InstructionImpl {
  MaybeReturnInstruction(LuaExpression element, int num) {
    super(element, num);
  }

  public String toString() {
    return super.toString() + " MAYBE_RETURN";
  }

  public boolean mayReturnValue() {
    LuaExpression expression = (LuaExpression) getElement();
    assert expression != null;
    final LuaType type = expression.getLuaType();
    return type == null || type != LuaType.NIL;
  }

}
