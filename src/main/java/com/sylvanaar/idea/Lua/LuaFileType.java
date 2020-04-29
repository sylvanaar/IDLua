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

public class LuaFileType extends LanguageFileType {
    public static final String DEFAULT_EXTENSION = "lua";
    public static final String LUA = "Lua";
    public static final String LUA_PLUGIN_ID = LUA;
    public static final LuaLanguage LUA_LANGUAGE = new LuaLanguage();

    public static final FileType getFileType() { return LUA_LANGUAGE.getAssociatedFileType(); }

    public static final ExtensionFileNameMatcher[] EXTENSION_FILE_NAME_MATCHERS = {
            new ExtensionFileNameMatcher(LuaFileType.DEFAULT_EXTENSION),
            new ExtensionFileNameMatcher("doclua"),
            new ExtensionFileNameMatcher("wlua"),
    };

    protected LuaFileType() {
        super(LUA_LANGUAGE);
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



