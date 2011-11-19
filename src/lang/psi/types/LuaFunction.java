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
 * Time: 7:20 AM
 */
public class LuaFunction extends LuaType {
//    List<LuaList> args;
//    List<LuaList> rets;

    Set<LuaType> ret1 = new HashSet<LuaType>();

    @Override
    public String toString() {
        return String.format("{function: returns %s}", ret1);
    }

    public void addPossibleReturn(LuaType firstReturn) {
//        rets.add(returns)

        ret1.remove(LuaType.ANY);
        ret1.add(firstReturn != null ? firstReturn : LuaType.NIL);
    }
    
    public LuaType getReturnType() {
        if (ret1.isEmpty()) return LuaType.ANY;

        return new LuaTypeSet(ret1);
    }
}

