/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.symbols;

import com.intellij.navigation.NavigationItem;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/26/11
 * Time: 6:06 PM
 */
public interface LuaSymbol extends LuaExpression, LuaNamedElement, NavigationItem {
    static final LuaSymbol[] EMPTY_ARRAY = new LuaSymbol[0];
    
    public boolean isSameKind(LuaSymbol symbol);

    public boolean isAssignedTo();
}
