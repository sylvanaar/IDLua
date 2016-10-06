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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 9/18/11
 * Time: 7:20 AM
 */
public class LuaFunction extends LuaTypeImpl implements LuaNamespacedType {

    static final         Logger log              = Logger.getInstance("Lua.LuaFunction");
    private static final long   serialVersionUID = -7837667402823310798L;
//    List<LuaList> args;
//    List<LuaList> rets;

    Set<LuaType> ret1 = new HashSet<LuaType>(5);
    private String myNamespace;

    @Override
    public String toString() {
        return String.format("{function: %s}", getEncodedAsString());
    }

    public synchronized void addPossibleReturn(LuaType firstReturn) {
        if (firstReturn.equals(LuaPrimitiveType.ANY))
            return;
//        rets.add(returns)

        ret1.remove(LuaPrimitiveType.ANY);
        ret1.add(firstReturn);

        // log.debug("New return of function: " + type);
    }

    public LuaType getReturnType() {
        if (ret1.isEmpty())
            return LuaPrimitiveType.ANY;

        if (ret1.size() == 1)
            return ret1.iterator().next();

        return new LuaTypeSet(ret1);
    }

    @Override
    public String encode(Map<LuaType, String> encodingContext) {
        if (encodingContext.containsKey(this)) return encodingContext.get(this);
        encodingContext.put(this,  "!RECURSION!");

        StringBuilder sb = new StringBuilder(30);
        sb.append(StringUtil.notNullize(myNamespace, "<anon>"));
        sb.append(":(");
        for (LuaType type : ret1)
            sb.append(type.encode(encodingContext));
        sb.append(')');

        return encodingResult(encodingContext, sb.toString());
    }

    public synchronized void reset() {
        ret1.clear();
    }

    @Override
    public String getNamespace() {
        return myNamespace;
    }

    @Override
    public void setNamespace(String namespace) {
        myNamespace = namespace;
    }
}

