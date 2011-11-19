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

package com.sylvanaar.idea.Lua.lang.psi.types;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 9/18/11
 * Time: 4:31 AM
 */
public class LuaTypeSet extends LuaType {
    Set<LuaType> possibleTypes;

    protected LuaTypeSet(LuaType type1, LuaType type2) {
        possibleTypes = new HashSet<LuaType>();

        addTypes(type1);
        addTypes(type2);
    }

    public LuaTypeSet(Set<LuaType> types) {
        possibleTypes = types;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TypeSet:");
        for(LuaType type : possibleTypes)
            sb.append(' ').append(type);
        return sb.toString();
    }

    private void addTypes(LuaType type1) {
        if (type1 instanceof LuaTypeSet)
            possibleTypes.addAll(((LuaTypeSet) type1).possibleTypes);
        else
            possibleTypes.add(type1);
    }
}
