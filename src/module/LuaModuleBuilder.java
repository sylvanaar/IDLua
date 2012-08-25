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

package com.sylvanaar.idea.Lua.module;

import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.module.*;
import com.intellij.openapi.options.*;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.*;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.*;

import java.util.*;

class LuaModuleBuilder extends ModuleBuilder  {

    @Nullable
    private String myContentRootPath = null;
    @Nullable
    private Sdk mySdk = null;

    public void setupRootModel(@NotNull final ModifiableRootModel rootModel) throws ConfigurationException {
        if (mySdk != null) {
            rootModel.setSdk(mySdk);
        } else {
            rootModel.inheritSdk();
        }
        if (myContentRootPath != null) {
            final LocalFileSystem lfs = LocalFileSystem.getInstance();
            //noinspection ConstantConditions
            final VirtualFile moduleContentRoot = lfs.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(myContentRootPath));
            if (moduleContentRoot != null) {
                rootModel.addContentEntry(moduleContentRoot);
            }
        }
    }

    @NotNull
    public ModuleType getModuleType() {
        return LuaModuleType.getInstance();
    }

    @Nullable
    public String getContentEntryPath() {
        return myContentRootPath;
    }

    public void setContentEntryPath(@Nullable final String contentRootPath) {
        myContentRootPath = contentRootPath;
    }

    public void setSdk(@Nullable final Sdk sdk) {
        mySdk = sdk;
    }
}
