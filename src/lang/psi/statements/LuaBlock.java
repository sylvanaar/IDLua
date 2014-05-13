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

package com.sylvanaar.idea.Lua.lang.psi.statements;

import com.sylvanaar.idea.Lua.lang.psi.LuaControlFlowOwner;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaStatementOwner;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaVariableDeclarationOwner;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 13, 2010
 * Time: 6:09:03 PM
 */
public interface LuaBlock extends LuaPsiElement, LuaVariableDeclarationOwner, LuaControlFlowOwner, LuaStatementOwner {
     public LuaStatementElement[] getStatements();
     LuaExpressionList getReturnedValue();
}
