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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 19, 2010
 * Time: 5:33:53 PM
 */

@State(
        name = "LuaApplicationSettings",
        storages = {
                @Storage(id = "other",
                        file = "$APP_CONFIG$/other.xml")
        }
)
public class LuaApplicationSettings implements PersistentStateComponent<LuaApplicationSettings> {
    public boolean INCLUDE_ALL_FIELDS_IN_COMPLETIONS = false;
    public boolean SHOW_TAIL_CALLS_IN_GUTTER = true;
    public boolean ENABLE_TYPE_INFERENCE = true;

    @Override
    public LuaApplicationSettings getState() {
        return this;
    }

    @Override
    public void loadState(LuaApplicationSettings state) {
         XmlSerializerUtil.copyBean(state, this);
    }

    public static LuaApplicationSettings getInstance() {
        return ServiceManager.getService(LuaApplicationSettings.class);
    }
}
