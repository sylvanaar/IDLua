/*
 * Copyright 2009 Joachim Ansorg, mail@ansorg-it.com
 * File: LuaFacetLoader.java, Class: LuaFacetLoader
 * Last modified: 2010-02-11
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.settings.facet;

import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class LuaFacetLoader implements ApplicationComponent {
    //public static final String PLUGIN_MODULE_ID = "PLUGIN_MODULE";

    public static LuaFacetLoader getInstance() {
        return ApplicationManager.getApplication().getComponent(LuaFacetLoader.class);
    }

    public LuaFacetLoader() {
    }

    public void initComponent() {
        FacetTypeRegistry.getInstance().registerFacetType(LuaFacetType.INSTANCE);
    }

    public void disposeComponent() {
        FacetTypeRegistry instance = FacetTypeRegistry.getInstance();
        instance.unregisterFacetType(instance.findFacetType(LuaFacetType.ID));
    }

    @NotNull
    public String getComponentName() {
        return "LuaFacetLoader";
    }
}
