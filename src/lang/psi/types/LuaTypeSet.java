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
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 9/18/11
 * Time: 4:31 AM
 */
public class LuaTypeSet extends LuaType {
    public LuaTypeSet() { possibleTypes = new HashSet<LuaType>(); }

    Set<LuaType> possibleTypes;

    protected LuaTypeSet(LuaType type1, LuaType type2) {
        this();

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
            if (type != this)
                sb.append(' ').append(type).append(" Encoded: ").append(type.getEncodedAsString());
        return sb.toString();
    }

    @Override
    protected String encode(Map<LuaType, String> encodingContext)  {
        if (encodingContext.containsKey(this)) return encodingContext.get(this);
        encodingContext.put(this,  "!RECURSION!");

        if (possibleTypes.size() == 0) return  encodingResult(encodingContext, LuaType.ANY.encode(encodingContext));
        if (possibleTypes.size() == 1) return  encodingResult(encodingContext, possibleTypes.iterator().next().encode(encodingContext));

        StringBuilder sb = new StringBuilder();

        sb.append('[');
        for(LuaType type : possibleTypes)
            if (type != this)
                sb.append(type.encode(encodingContext));
        sb.append(']');

        return encodingResult(encodingContext, sb.toString());
    }

//    public static LuaType decode(Scanner scanner) {
//        Set<LuaType> possibleTypes = new HashSet<LuaType>();
//
//        while (scanner.hasNext(".")) {
//            LuaType type = LuaType.decode(scanner);
//        }
//
//        return type;
//    }

    private void addTypes(LuaType type1) {
        if (type1 instanceof LuaTypeSet)
            possibleTypes.addAll(((LuaTypeSet) type1).possibleTypes);
        else
            possibleTypes.add(type1);
    }
}
