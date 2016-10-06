/*
 * Copyright 2009 Joachim Ansorg, mail@ansorg-it.com
 * File: LuaFacetTypeFrameworkSupportProvider.java, Class: LuaFacetTypeFrameworkSupportProvider
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

package com.sylvanaar.idea.Lua.facet;

import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.sylvanaar.idea.Lua.module.LuaModuleType;
import org.jetbrains.annotations.NotNull;

public class LuaFacetFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<LuaFacet> {
    protected LuaFacetFrameworkSupportProvider() {
        super(LuaFacetType.INSTANCE);
    }

    @Override
    public String getTitle() {
        return "Lua";
    }

    @Override
    protected void setupConfiguration(LuaFacet LuaFacet, ModifiableRootModel modifiableRootModel, FrameworkVersion frameworkVersion) {

    }

    @Override
    public boolean isEnabledForModuleType(@NotNull ModuleType moduleType) {
        return !(moduleType instanceof LuaModuleType);
    }
}