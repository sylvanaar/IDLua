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

package com.sylvanaar.idea.Lua.library;

import com.intellij.framework.library.DownloadableLibraryTypeBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.module.LuaModuleType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/21/11
 * Time: 8:54 PM
 */
public class LuaLibraryType extends DownloadableLibraryTypeBase {

    public static final String LUA_LIBRARY_TYPE_ID = "Lua";
    public static final String LUA_LIBRARY_CATEGORY_NAME = "Lua";

    public LuaLibraryType() {
        super(LUA_LIBRARY_CATEGORY_NAME, "Lua", "Lua",
                LuaIcons.LUA_ICON, null);
    }

    @NotNull
    @Override
    public String getCreateActionName() {
        return "New Lua Library";
    }

    @NotNull
    @Override
    public LuaLibraryProperties createDefaultProperties() {
        return new LuaLibraryProperties();
    }

    @Override
    public Icon getIcon() {
        return LuaIcons.LUA_ICON;
    }

    @Override
    protected String[] getDetectionClassNames() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSuitableModule(@NotNull Module module, @NotNull FacetsProvider facetsProvider) {
        if (module instanceof LuaModuleType) return true;


        return false;
    }


}
