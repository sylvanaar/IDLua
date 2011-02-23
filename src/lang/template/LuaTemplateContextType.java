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

package com.sylvanaar.idea.Lua.lang.template;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.editor.highlighter.LuaSyntaxHighlighter;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/22/11
 * Time: 10:45 PM
 */
public class LuaTemplateContextType extends TemplateContextType {
    protected LuaTemplateContextType() {
        super("LUA", "Lua");
    }

    public boolean isInContext(@NotNull PsiFile file, int offset) {
        return file instanceof LuaPsiFile;
    }

    public boolean isInContext(@NotNull FileType fileType) {
        return fileType instanceof LuaFileType;
    }

    @Override
    public SyntaxHighlighter createHighlighter() {
        return new LuaSyntaxHighlighter();
    }
}

