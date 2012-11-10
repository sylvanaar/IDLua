/*
 * Copyright 2012 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.types;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 7/2/12
 * Time: 7:28 PM
 */
public class LuaTypeUtil {
    public static LuaType combineTypes(LuaType type1, LuaType type2) {
        if (type1.equals(type2)) return type1;
        if (type1.equals(LuaPrimitiveType.ANY)) return type2;
        if (type2.equals(LuaPrimitiveType.ANY)) return type1;
        return new LuaTypeSet(type1, type2);
    }
}

