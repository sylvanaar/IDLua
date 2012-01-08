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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.LibraryKind;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.module.LuaModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/21/11
 * Time: 8:54 PM
 */
public class LuaLibraryType extends LibraryType<LibraryProperties> {

    public static final String LUA_LIBRARY_TYPE_ID = "Lua";
    public static final String LUA_LIBRARY_CATEGORY_NAME = "Lua";

    public LuaLibraryType() {
        super(LibraryKind.create("Lua"));
    }

    @NotNull
    @Override
    public String getCreateActionName() {
        return "New Lua Library";
    }

    @Override
    public NewLibraryConfiguration createNewLibrary(@NotNull JComponent jComponent, @Nullable VirtualFile virtualFile,
                                                    @NotNull Project project) {
        return null;
    }

    @NotNull
    @Override
    public LibraryProperties createDefaultProperties() {
        return new LuaLibraryProperties();
    }

    @Override
    public Icon getIcon() {
        return LuaIcons.LUA_ICON;
    }

    @Override
    public boolean isSuitableModule(@NotNull Module module, @NotNull FacetsProvider facetsProvider) {
        if (module instanceof LuaModuleType) return true;


        return false;
    }

    @Override
    public LibraryPropertiesEditor createPropertiesEditor(@NotNull LibraryEditorComponent<LibraryProperties>
                                                                  libraryPropertiesLibraryEditorComponent) {

        return null;
    }


}
