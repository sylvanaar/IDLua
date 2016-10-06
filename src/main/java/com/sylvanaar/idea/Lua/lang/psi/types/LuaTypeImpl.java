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

import com.intellij.ide.plugins.*;
import com.intellij.openapi.extensions.*;
import com.sylvanaar.idea.Lua.util.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/29/11
 * Time: 6:59 PM
 */
public abstract class LuaTypeImpl implements LuaType {
    private static final long serialVersionUID = 223704448740680847L;

    protected LuaTypeImpl() { }

    @Override
    public String toString() {
        return "Unknown Type";
    }

    @Override
    public final String getEncodedAsString() {
        return encode(new HashMap<LuaType, String>(10));
    }

    @Override
    public LuaType getFromEncodedString(byte... input) {
        if (input == null || input.length == 0)
            return LuaPrimitiveType.ANY;

        ClassLoader classLoader = PluginManager.getPlugin(PluginId.getId("Lua")).getPluginClassLoader();

        Object result = LuaSerializationUtils.deserialize(input, classLoader);

        assert result instanceof LuaType;

        return (LuaType) result;
    }


    @Override
    public String encodingResult(Map<LuaType, String> encodingContext, String encoded) {
        encodingContext.put(this,  encoded);
        return encoded;
    }

    @Override
    public  LuaType combineTypes(LuaType type1, LuaType type2) {
        return LuaTypeUtil.combineTypes(type1, type2);
    }



}
