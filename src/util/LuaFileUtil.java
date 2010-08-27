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

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.LuaFileType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Maxim.Manuylov
 *         Date: 07.04.2010
 */
public class LuaFileUtil {
    public static boolean isLuaSourceFile(@NotNull final FileType fileType) {
        return fileType == LuaFileType.LUA_FILE_TYPE;
    }

    public static boolean isLuaSourceFile(@NotNull final VirtualFile file) {
        return isLuaSourceFile(file.getFileType());
    }

    public static boolean isLuaFile(@NotNull final VirtualFile file) {
        final String extension = file.getExtension();
        return isLuaSourceFile(file);
    }

    @NotNull
    public static File getCompiledDir(@NotNull final ProjectFileIndex fileIndex, @NotNull final VirtualFile sourcesDir) {
        final VirtualFile sourceRoot = fileIndex.getSourceRootForFile(sourcesDir);
        assert sourceRoot != null && sourceRoot.isDirectory();
        final String sourceRootPath = sourceRoot.getPath();

        final ArrayList<String> relativeDirs = new ArrayList<String>();
        VirtualFile parent = sourcesDir;
        while (parent != null && !parent.getPath().equals(sourceRootPath)) {
            relativeDirs.add(0, parent.getName());
            parent = parent.getParent();
        }
        assert parent != null;

        final Module module = fileIndex.getModuleForFile(sourcesDir);
        assert module != null;
        final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
        assert compilerModuleExtension != null;
        final VirtualFile outputRoot = compilerModuleExtension.getCompilerOutputPath();
        assert outputRoot != null && outputRoot.isDirectory();

        File destDir = new File(outputRoot.getPath());
        for (final String dirName : relativeDirs) {
            destDir = new File(destDir, dirName);
        }

        return destDir;
    }

//    @NotNull
//    public static String getAnotherFileName(@NotNull final VirtualFile file) {
//        final FileType type = file.getFileType();
//        assert type instanceof LuaFileType;
//        return getFileName(file.getNameWithoutExtension(), ((LuaFileType) type).getAnotherFileType());
//    }

    @NotNull
    public static String getFileName(@NotNull final String nameWithoutExtension, @NotNull final FileType type) {
        return nameWithoutExtension + "." + type.getDefaultExtension();
    }

//    @Nullable
//    public static VirtualFile getAnotherFile(@Nullable final VirtualFile file) {
//        if (file == null) return null;
//        final VirtualFile parent = file.getParent();
//        if (parent == null) return null;
//        return parent.findChild(getAnotherFileName(file));
//    }

//    public static boolean isImplementationFile(@NotNull final VirtualFile file) {
//        return file.getFileType() == MLFileType.INSTANCE;
//    }
//
//    public static boolean isImplementationFile(@NotNull final PsiFile file) {
//        return file.getFileType() == MLFileType.INSTANCE;
//    }

    @NotNull
    public static String getPathToDisplay(@NotNull final VirtualFile file) {
        return FileUtil.toSystemDependentName(file.getPath());
    }
}
