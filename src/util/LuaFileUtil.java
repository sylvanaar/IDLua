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

package com.sylvanaar.idea.Lua.util;

import com.intellij.ide.plugins.*;
import com.intellij.openapi.extensions.*;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.io.*;
import com.intellij.openapi.vfs.*;
import com.sylvanaar.idea.Lua.*;
import org.jetbrains.annotations.*;

import java.io.*;

/**
 * @author Maxim.Manuylov
 *         Date: 07.04.2010
 */
public class LuaFileUtil {
    @NotNull
    public static String getPathToDisplay(final VirtualFile file) {
        if (file == null) {
            return "";
        }
        return FileUtil.toSystemDependentName(file.getPath());
    }


    @Nullable
    public static VirtualFile getPluginVirtualDirectory() {
        IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId(LuaFileType.LUA_PLUGIN_ID));
        if (descriptor != null) {
            File pluginPath = descriptor.getPath();

            String url = VfsUtil.pathToUrl(pluginPath.getAbsolutePath());

            return VirtualFileManager.getInstance().findFileByUrl(url);
        }

        return null;
    }


    public static boolean iterateRecursively(@Nullable final VirtualFile root, @NotNull final ContentIterator processor) {
        return root != null && VfsUtilCore.iterateChildrenRecursively(root, VirtualFileFilter.ALL, processor);
    }

    public static boolean iterateLuaFilesRecursively(@Nullable final VirtualFile root, @NotNull final ContentIterator
            processor) {
        return root != null && VfsUtilCore.iterateChildrenRecursively(root, LUA_FILE_FILTER, processor);
    }

    static VirtualFileFilter LUA_FILE_FILTER = new VirtualFileFilter() {
        @Override
        public boolean accept(VirtualFile file) {
            if (file.isDirectory()) return true;

            for (ExtensionFileNameMatcher matcher : LuaFileType.EXTENSION_FILE_NAME_MATCHERS) {
                if (matcher.accept(file.getName())) return true;
            }

            return false;
        }

        public String toString() {
            return "LUA";
        }
    };
}
