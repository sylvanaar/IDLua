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

import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.*;
import com.intellij.openapi.roots.libraries.ui.*;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.*;
import com.intellij.openapi.vfs.*;
import com.sylvanaar.idea.Lua.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/21/11
 * Time: 8:54 PM
 */
public class LuaLibraryType extends LibraryType<LuaLibraryProperties> implements LuaLibrary {
    private static final PersistentLibraryKind<LuaLibraryProperties> LIBRARY_KIND =
            new PersistentLibraryKind<LuaLibraryProperties>(LUA_LIBRARY_KIND_ID) {
                @NotNull
                @Override
                public LuaLibraryProperties createDefaultProperties() {
                    return new LuaLibraryProperties();
                }
            };

    protected LuaLibraryType() {
        super(LIBRARY_KIND);
    }


    @NotNull
    @Override
    public String getCreateActionName() {
        return "New Lua Library";
    }

    @Override
    public NewLibraryConfiguration createNewLibrary(@NotNull JComponent jComponent, @Nullable VirtualFile virtualFile,
                                                    @NotNull Project project) {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createAllButJarContentsDescriptor();
        descriptor.setTitle(LuaBundle.message("new.library.file.chooser.title"));
        descriptor.setDescription(LuaBundle.message("new.library.file.chooser.description"));
        final VirtualFile[] files = FileChooser.chooseFiles(descriptor, project, virtualFile);

        if (files.length == 0) {
            return null;
        }
        return new NewLibraryConfiguration("Lua Library", this, new LuaLibraryProperties()) {
            @Override
            public void addRoots(@NotNull LibraryEditor editor) {
                for (VirtualFile file : files) {
                    editor.addRoot(file, OrderRootType.CLASSES);
                }
            }
        };
    }
//
//    @NotNull
//    @Override
//    public LuaLibraryProperties createDefaultProperties() {
//        return new LuaLibraryProperties();
//    }

    @Nullable
    @Override
    public Icon getIcon(@Nullable LuaLibraryProperties luaLibraryProperties) {
        return LuaIcons.LUA_ICON;
    }
//    @Override
//    public LibraryRootsComponentDescriptor createLibraryRootsComponentDescriptor() {
//        return new LuaLibraryRootsComponentDescriptor();
//    }

    @Override
    public LibraryPropertiesEditor createPropertiesEditor(@NotNull LibraryEditorComponent<LuaLibraryProperties>
                                                                  libraryPropertiesLibraryEditorComponent) {

        return null;
    }

    @Override
    public LuaLibraryProperties detect(@NotNull List<VirtualFile> classesRoots) {
        for (VirtualFile vf : classesRoots) {
            if (!vf.isDirectory())
                return null;

            for(VirtualFile file : vf.getChildren()) {
                String fileExtension = file.getExtension();
                if (fileExtension != null)
                    if (fileExtension.equals(LuaFileType.DEFAULT_EXTENSION))
                        return new LuaLibraryProperties();
            }
        }

        return null;
    }

}
