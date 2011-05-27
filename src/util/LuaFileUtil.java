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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Maxim.Manuylov
 *         Date: 07.04.2010
 */
public class LuaFileUtil {
    @NotNull
    public static String getPathToDisplay(@NotNull final VirtualFile file) {
        return FileUtil.toSystemDependentName(file.getPath());
    }


    @Nullable
    public static VirtualFile getPluginVirtualDirectory() {
        IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("Lua"));
        if (descriptor != null) {
            File pluginPath = descriptor.getPath();

            String url = VfsUtil.pathToUrl(pluginPath.getAbsolutePath());

            return VirtualFileManager.getInstance().findFileByUrl(url);
        }

        return null;
    }


    public static boolean iterateRecursively(@Nullable final VirtualFile root, @NotNull final ContentIterator processor) {
        if (root != null) {
            if (root.isDirectory()) {
                for (VirtualFile file : root.getChildren()) {
                    if (file.isDirectory()) {
                        if (!iterateRecursively(file, processor)) return false;
                    } else {
                        if (!processor.processFile(file)) return false;
                    }
                }
            } else {
                if (!processor.processFile(root)) return false;
            }
        }
        return true;
    }
}
