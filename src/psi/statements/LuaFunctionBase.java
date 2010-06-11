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

import com.sylvanaar.idea.Lua.psi.LuaIdentifier;
import com.sylvanaar.idea.Lua.psi.LuaParameterList;
import com.sylvanaar.idea.Lua.psi.LuaPsiElement;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 11, 2010
 * Time: 3:52:12 AM
 */
public interface LuaFunctionBase extends LuaPsiElement {
    LuaIdentifier getIdentifier();

    LuaParameterList getParameters();
}
