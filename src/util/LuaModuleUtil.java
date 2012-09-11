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

import com.intellij.openapi.module.*;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.*;
import com.sylvanaar.idea.Lua.*;
import com.sylvanaar.idea.Lua.module.*;
import com.sylvanaar.idea.Lua.sdk.*;
import org.jetbrains.annotations.*;

/**
 * @author Maxim.Manuylov
 *         Date: 29.04.2010
 */
public class LuaModuleUtil {
    public static boolean isLuaSdk(@Nullable final Sdk sdk) {
        return sdk != null && sdk.getSdkType() instanceof LuaSdkType;
    }

    public static boolean isLuaModule(@Nullable final Module module) {
        return module != null && LuaModuleType.ID.equals(ModuleType.get(module).getId());
    }

//    public static void checkForSdkFile(final LuaPsiFile file, Project project) {
//        ModuleManager mm = ModuleManager.getInstance(project);
//        boolean isSdkFile = false;
//
//        for (final Module module : mm.getModules()) {
//            ModuleRootManager mrm = ModuleRootManager.getInstance(module);
//            Sdk sdk = mrm.getSdk();
//
//            if (sdk != null) {
//                VirtualFile[] vf = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);
//
//                for (VirtualFile libraryFile : vf)
//                    LuaFileUtil.iterateRecursively(libraryFile, new ContentIterator() {
//                        @Override
//                        public boolean processFile(VirtualFile virtualFile) {
//                            if (file.getVirtualFile() == virtualFile) {
//                                file.setSdkFile(true);
//                                return false;
//                            }
//                            return true;
//                        }
//                    });
//            }
//        }
//    }

    public static boolean isLuaContentFile(ProjectFileIndex myIndex, VirtualFile file) {
        final String extension = file.getExtension();
        if (extension == null) return false;

        return extension.equals(LuaFileType.DEFAULT_EXTENSION) &&
               (myIndex.isInContent(file) || myIndex.isInLibraryClasses(file));
    }
}
