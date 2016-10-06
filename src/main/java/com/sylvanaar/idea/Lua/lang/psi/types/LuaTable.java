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

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 9/18/11
 * Time: 3:08 AM
 */
public class LuaTable extends LuaTypeImpl implements LuaNamespacedType {
    static final         Logger log              = Logger.getInstance("Lua.LuaTable");
    private static final long   serialVersionUID = 1827449546740623111L;

    final private Map<String, LuaType> hash = new HashMap<String, LuaType>();

    private String myNamespace;

    public LuaTable() { myNamespace = null;}

    @Override
    public String toString() {
        return "Table: " + getEncodedAsString();
    }

    @Override
    public String encode(Map<LuaType, String> encodingContext) {
        if (encodingContext.containsKey(this))
            return encodingContext.get(this);
        encodingContext.put(this, "!RECURSION!");

        StringBuilder sb = new StringBuilder(25);

        sb.append(StringUtil.notNullize(myNamespace, "<anon>"));
        sb.append(":{");
        synchronized (hash) {
            for (Map.Entry<String, LuaType> type : hash.entrySet()) {
                final LuaType value = type.getValue();
                if (value != null && !value.equals(this))
                    sb.append('@').append(type.getKey()).append('=').append(value.encode(encodingContext));
            }
        }
        sb.append('}');

        return encodingResult(encodingContext, sb.toString());
    }

   private void writeObject(java.io.ObjectOutputStream out) throws IOException {
       synchronized (hash) {
           out.defaultWriteObject();
       }
   }

    public void addPossibleElement(Object key, LuaType type) {
        assert type != null : "Null type for " + key;


        String keyString = key instanceof LuaNamedElement ? ((NavigationItem) key).getName() : key.toString();
        synchronized (hash) {
            final LuaType current = hash.get(keyString);
            hash.put(keyString, current != null ? combineTypes(current, type) : type);
        }
    }

    public Map<String,? extends LuaType> getFieldSet() {
        return Collections.unmodifiableMap(hash);
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
