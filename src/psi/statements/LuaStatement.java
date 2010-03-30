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


import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.psi.auxiliary.LuaCondition;
import com.sylvanaar.idea.Lua.psi.toplevel.LuaTopStatement;

/**
 * @author: Dmitry.Krasilschikov
 * @date: 21.03.2007
 */
public interface LuaStatement extends LuaTopStatement, LuaCondition {
  public static final LuaStatement[] EMPTY_ARRAY = new LuaStatement[0];
  
  <T extends LuaStatement> T replaceWithStatement (T statement);

  void removeStatement() throws IncorrectOperationException;
}
