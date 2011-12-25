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

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 9/18/11
 * Time: 11:06 AM
 */
public class LuaList extends LuaType {
    LuaType[] typeList;

    public LuaList(LuaType... types) {
        typeList = types;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TypeList: ").append(getEncodedAsString());

        return sb.toString();
    }

    @Override
    public String getEncodedAsString() {
        if (typeList.length == 0) return LuaType.ANY.getEncodedAsString();
        if (typeList.length == 1) return typeList[0].getEncodedAsString();

        StringBuilder sb = new StringBuilder();

        sb.append('<');
        for (LuaType type : typeList)
            sb.append(type != null ? type.getEncodedAsString() : "!ERR!");
        sb.append('>');

        return sb.toString();
    }

}
