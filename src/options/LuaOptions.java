/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.options;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NonNls;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 20, 2010
 * Time: 8:19:16 PM
 */
public class LuaOptions {
    Map<String, LuaOption> props = new HashMap<String, LuaOption>();

    private LuaOptions() {}


    public void loadValue(@NonNls String name, String defaultValue) {
        if (PropertiesComponent.getInstance().isValueSet(name))
         setValue(name, PropertiesComponent.getInstance().getValue(name));
        else
         setValue(name, defaultValue);
    }

    public String getValue(@NonNls String name) {
       return props.get(name).getValue();
    }

    public void setValue(@NonNls String name, String value) {
       props.get(name).setValue(value);

       PropertiesComponent.getInstance().setValue(name, value);
    }

    static LuaOptions INSTANCE = null;
    public static LuaOptions getInstance() {
        if (INSTANCE == null)
        INSTANCE = new LuaOptions();

        return INSTANCE;
    }

    public void registerOption(String name, LuaOption option) {
        props.put(name, option);
    }
}
