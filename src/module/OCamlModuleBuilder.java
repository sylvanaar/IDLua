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
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

class LuaModuleBuilder extends ModuleBuilder implements SourcePathsBuilder {
    @NotNull private String myRelativeSourcesPath = "src";
    private boolean myShouldCreateSourcesDir = true;
    @Nullable private String myContentRootPath = null;
    @Nullable private Sdk mySdk = null;

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
                final ContentEntry contentEntry = rootModel.addContentEntry(moduleContentRoot);
                if (myShouldCreateSourcesDir) {
                    final File sourcesDir = getSourcesDir();
                    if (!sourcesDir.isDirectory()) {
                        //noinspection ResultOfMethodCallIgnored
                        sourcesDir.mkdirs();
                    }
                    final VirtualFile sourceRoot = lfs.refreshAndFindFileByIoFile(sourcesDir);
                    if (sourceRoot != null) {
                        contentEntry.addSourceFolder(sourceRoot, false, "");
                    }
                }
            }
        }
    }

    @NotNull
    private File getSourcesDir() {
        final String[] dirs = myRelativeSourcesPath.replace("\\", "/").split("/");
        File result = new File(myContentRootPath);
        for (final String dir : dirs) {
            result = new File(result, dir);
        }
        return result;
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

//    public boolean isShouldCreateSourcesDir() {
//        return myShouldCreateSourcesDir;
//    }
//
//    public void setShouldCreateSourcesDir(final boolean shouldCreateSourcesDir) {
//        myShouldCreateSourcesDir = shouldCreateSourcesDir;
//    }
//
//    @NotNull
//    public String getRelativeSourcesPath() {
//        return myRelativeSourcesPath;
//    }
//
//    public void setRelativeSourcesPath(@NotNull final String relativeSourcesPath) {
//        myRelativeSourcesPath = relativeSourcesPath;
//    }
//
//    public void setSdk(@Nullable final Sdk sdk) {
//        mySdk = sdk;
//    }

    public List<Pair<String, String>> getSourcePaths() {
        throw new UnsupportedOperationException();
    }

    public void setSourcePaths(final List<Pair<String, String>> sourcePaths) {
        throw new UnsupportedOperationException();
    }

    public void addSourcePath(final Pair<String, String> sourcePathInfo) {
        throw new UnsupportedOperationException();
    }
}
