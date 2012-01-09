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

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.sylvanaar.idea.Lua.util.LuaSerializationUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/29/11
 * Time: 6:59 PM
 */
public class LuaType implements Serializable {
    public static final LuaType BOOLEAN = new LuaType("BOOLEAN", "B");
    public static final LuaType NUMBER = new LuaType("NUMBER", "N");
    public static final LuaType STRING = new LuaType("STRING", "S");
    public static final LuaType USERDATA = new LuaType("USERDATA", "U");
    public static final LuaType LIGHTUSERDATA = new LuaType("LIGHTUSERDATA", "L");
    public static final LuaType NIL = new LuaType("NIL", "0");
    public static final LuaType THREAD = new LuaType("THREAD", "X");
    public static final LuaType ANY = new LuaType("ANY", "*");
    public static final LuaType ERROR = new LuaType("ERROR");

    public static final LuaType STUB = new LuaType("STUB", "STUB");

    private String name;
    private String encodedString;
    
    protected LuaType(String name){ this.name = name; }
    protected LuaType(String name, String encoded){ this.name = name; this.encodedString = encoded; }

    public LuaType() { this.name = "{unknown}"; }

    @Override
    public String toString() {
        return name;   
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LuaType && obj.toString().equals(toString());
    }

    public final String getEncodedAsString() {
        return encode(new HashMap<LuaType, String>());
    }
    
    public static LuaType getFromEncodedString(byte[] input) {
        if (input == null || input.length == 0) return LuaType.ANY;

        ClassLoader classLoader = PluginManager.getPlugin(PluginId.getId("Lua")).getPluginClassLoader();
        
        Object result = LuaSerializationUtils.deserialize(input, classLoader);

        assert result instanceof LuaType;

        return (LuaType) result;
    }
    
    protected String encode(Map<LuaType, String> encodingContext) { return encodedString; }


    protected String encodingResult(Map<LuaType, String> encodingContext, String encoded) {
        encodingContext.put(this,  encoded);
        return encoded;
    }

    public static LuaType combineTypes(LuaType type1, LuaType type2) {
        if (type1 == type2) return type1;
        if (type1 == LuaType.ANY) return type2;
        if (type2 == LuaType.ANY) return type1;
        return new LuaTypeSet(type1, type2);
    }

}
