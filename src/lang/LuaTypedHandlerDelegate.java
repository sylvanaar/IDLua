/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.lang;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/30/11
 * Time: 6:45 AM
 */
public class LuaTypedHandlerDelegate extends TypedHandlerDelegate {
    @Override
    public Result beforeCharTyped(char c, Project project,
                                  Editor editor, PsiFile file, FileType fileType) {
        Document document = editor.getDocument();
        CharSequence text = document.getCharsSequence();
        int caretOffset = editor.getCaretModel().getOffset();

        if ((text.subSequence(caretOffset - 5, caretOffset).toString().equals("until"))) {
            int i = CodeStyleManager.getInstance(file.getProject()).
                                adjustLineIndent(file, caretOffset - 5);
                        editor.getCaretModel().moveToOffset(i + 5);
        }
        
        return super.beforeCharTyped(c, project, editor, file, fileType);
    }
}
