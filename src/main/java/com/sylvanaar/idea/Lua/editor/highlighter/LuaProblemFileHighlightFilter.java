/*
 * Copyright 2012 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.LuaFileType;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 11/7/12
 * Time: 12:58 AM
 */
public class LuaProblemFileHighlightFilter implements Condition<VirtualFile> {
    @Override public boolean value(VirtualFile virtualFile) {
        return virtualFile.getFileType() == LuaFileType.LUA_FILE_TYPE;
    }
}
