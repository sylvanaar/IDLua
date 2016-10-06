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


import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class LuaModuleBuilder extends ModuleBuilder {
    @Nullable
    private String myContentRootPath = null;
    @Nullable
    private Sdk    mySdk             = null;

    @Nullable
    public Sdk getSdk() { return mySdk; }

    public void setSdk(@Nullable Sdk mySdk) {
        this.mySdk = mySdk;
    }

    @Override
    @Nullable
    public String getContentEntryPath() { return myContentRootPath; }

    @Override
    public void setContentEntryPath(@Nullable final String contentRootPath) {
        myContentRootPath = contentRootPath;
    }

    @Override
    public void setupRootModel(@NotNull final ModifiableRootModel rootModel) throws ConfigurationException {
        if (mySdk == null) {
            rootModel.inheritSdk();
        } else {
            rootModel.setSdk(mySdk);
        }

        // Make the entire module directory a source root.
        ContentEntry contentEntry = doAddContentEntry(rootModel);
        if (contentEntry != null) {
            final VirtualFile file = contentEntry.getFile();
            if (file != null && file.isDirectory())
                contentEntry.addSourceFolder(file, false);
        }
    }

    @Override
    @NotNull
    public ModuleType getModuleType() { return LuaModuleType.getInstance(); }


    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        return sdkType instanceof LuaSdkType;
    }

    @Override
    public boolean isSuitableSdk(Sdk sdk) { return sdk.getSdkType() == LuaSdkType.getInstance(); }
}
