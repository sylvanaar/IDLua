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

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 9/18/11
 * Time: 11:06 AM
 */
public class LuaList extends LuaTypeImpl {
    private static final long serialVersionUID = 3615852019810811265L;
    LuaType[] typeList = null;

    public LuaList(LuaType... types) {
        typeList = types;
    }

    @Override
    public String toString() {
        return "TypeList: " + getEncodedAsString();
    }

    @Override
    public String encode(Map<LuaType, String> encodingContext) {
        if (encodingContext.containsKey(this))
            return encodingContext.get(this);
        encodingContext.put(this, "!RECURSION!");

        if (typeList.length == 0)
            return encodingResult(encodingContext, LuaPrimitiveType.ANY.encode(encodingContext));
        if (typeList.length == 1)
            return encodingResult(encodingContext, typeList[0].encode(encodingContext));

        StringBuilder sb = new StringBuilder(20);

        sb.append('<');
        for (LuaType type : typeList)
            sb.append(type != null ? type.encode(encodingContext) : "!ERR!");
        sb.append('>');

        return encodingResult(encodingContext, sb.toString());
    }


}
