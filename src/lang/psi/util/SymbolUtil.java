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

package com.sylvanaar.idea.Lua.lang.psi.util;

import com.intellij.openapi.util.text.StringUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaModuleExpression;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobal;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 10/20/11
 * Time: 11:18 AM
 */
public class SymbolUtil {
    public static String getGlobalEnvironmentName(LuaGlobal global) {
        String module = global.getModuleName();
        String name = global.getName();

        if (StringUtil.isEmpty(name))
            return null;

        if (StringUtil.isEmpty(module))
            return name;

        if (name.equals("_M"))
            return module;

        if (name.equals("_G"))
            return "";

        return module + "." + name;
    }
    
    public static String getFullSymbolName(LuaSymbol s) {
        if (s instanceof LuaGlobal)
            return getGlobalEnvironmentName((LuaGlobal) s);
        
        return s.getName();
    }

    public static LuaModuleExpression getModule(LuaSymbol symbol) {
        return ((LuaPsiFile)symbol.getContainingFile()).getModuleAtOffset(symbol.getTextOffset());
    }
}
