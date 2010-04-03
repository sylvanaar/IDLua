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

package com.sylvanaar.idea.Lua.psi.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.psi.LuaPsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 07.07.2009
 * Time: 17:29:16
 */
public class LuaPsiFileImpl extends PsiFileBase implements LuaPsiFile {
    public LuaPsiFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, LuaFileType.LUA_LANGUAGE);
    }

    @NotNull
    public FileType getFileType() {
        return LuaFileType.LUA_FILE_TYPE;
    }

}
