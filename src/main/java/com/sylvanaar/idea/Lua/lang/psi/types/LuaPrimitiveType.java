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
 * Time: 7:04 PM
 */
public interface LuaPrimitiveType extends LuaType {
    LuaPrimitiveType ANY     = new LuaPrimitiveTypeImpl("ANY", "*", 0);
    LuaPrimitiveType NIL     = new LuaPrimitiveTypeImpl("NIL", "0", 1);
    LuaPrimitiveType BOOLEAN = new LuaPrimitiveTypeImpl("BOOLEAN", "B", 2);
    LuaPrimitiveType NUMBER  = new LuaPrimitiveTypeImpl("NUMBER", "N", 3);
    LuaPrimitiveType STRING  = new LuaPrimitiveTypeImpl("STRING", "S", 4);
    LuaType[] PRIMITIVE_TYPES = {ANY, NIL, BOOLEAN, NUMBER, STRING};


    int getId();
}
