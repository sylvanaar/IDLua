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

package com.sylvanaar.idea.Lua.lang.psi.types;

import com.intellij.psi.tree.IElementType;

import static com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes.LOGICAL_OPS;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/29/11
 * Time: 6:59 PM
 */
public class LuaType {
    public static final LuaType BOOLEAN = new LuaType("BOOLEAN");
    public static final LuaType NUMBER = new LuaType("NUMBER");
    public static final LuaType STRING = new LuaType("STRING");
    public static final LuaType USERDATA = new LuaType("USERDATA");
    public static final LuaType LIGHTUSERDATA = new LuaType("LIGHTUSERDATA");
    public static final LuaType NIL = new LuaType("NIL");
    public static final LuaType THREAD = new LuaType("THREAD");
    public static final LuaType ANY = new LuaType("ANY");

    public static final LuaType ERROR = new LuaType("ERROR");

    private String name;
    protected LuaType(String name){ this.name = name; }
    protected LuaType() { this.name = "{unknown}"; }
    @Override
    public String toString() {
        return name;   
    }


    public static LuaType combineTypes(LuaType type1, LuaType type2) {
        if (type1 == type2) return type1;
        if (type1 == LuaType.ANY) return type2;
        if (type2 == LuaType.ANY) return type1;
        return new LuaTypeSet(type1, type2);
    }

}
