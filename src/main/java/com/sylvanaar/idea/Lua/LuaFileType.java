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

package com.sylvanaar.idea.Lua;

import com.intellij.lang.*;
import com.intellij.openapi.fileTypes.*;
import com.sylvanaar.idea.Lua.lang.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 04.07.2009
 * Time: 1:03:43
 */
public class LuaFileType extends LanguageFileType {
    public static final LuaFileType LUA_FILE_TYPE = new LuaFileType();

    public static final Language LUA_LANGUAGE = LUA_FILE_TYPE.getLanguage();
    // public static final Icon LUA_FILE_TYPE = LuaIcons.LUA_ICON_16x16;
    @NonNls
    public static final String DEFAULT_EXTENSION = "lua";
    public static final String LUA = "Lua";
    public static final String LUA_PLUGIN_ID = LUA;

    public static final ExtensionFileNameMatcher[] EXTENSION_FILE_NAME_MATCHERS = {
        new ExtensionFileNameMatcher(LuaFileType.DEFAULT_EXTENSION), new ExtensionFileNameMatcher("doclua"), new ExtensionFileNameMatcher("wlua"),
    };

    private LuaFileType() {
        super(new LuaLanguage());
    }

    /**
     * Creates a language file type for the specified language.
     *
     * @param language The language used in the files of the type.
     */
    protected LuaFileType(@NotNull Language language) {
        super(language);
    }

    @NotNull
    public String getName() {
        return LUA;
    }

    @NotNull
    public String getDescription() {
        return LuaBundle.message("lua.filetype");
    }

    @NotNull
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    public Icon getIcon() {
        return LuaIcons.LUA_ICON;
    }

}



