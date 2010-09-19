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

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

import static com.intellij.util.xmlb.XmlSerializerUtil.copyBean;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 19, 2010
 * Time: 5:33:53 PM
 */

@State(
        name = "LuaSupportAppSettings",
        storages = {
                @Storage(id = "other",
                        file = "$APP_CONFIG$/other.xml")
        }
)
public class LuaOptionsComponent implements PersistentStateComponent<LuaOptions>, ApplicationComponent {
    private LuaOptions settings = new LuaOptions();
    @NotNull
    @Override
    public String getComponentName() {
        return "LuaOptionsComponent";
    }

    @Override
    public void initComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuaOptions getState() {
        return settings;
    }

    @Override
    public void loadState(LuaOptions state) {
        copyBean(state, settings);
    }
}
