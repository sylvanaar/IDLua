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

package com.sylvanaar.idea.Lua.lang;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/30/11
 * Time: 6:45 AM
 */
public class LuaTypedInsideBlockDelegate extends TypedHandlerDelegate {
    @Override
    public Result charTyped(char c, final Project project, final Editor editor, final PsiFile file) {
        Document document = editor.getDocument();
        int caretOffset = editor.getCaretModel().getOffset();

        PsiElement e = file.findElementAt(caretOffset);

        System.out.println(e);

        if (e instanceof LuaBlock) {
       //     LuaBlock block = (LuaBlock) e;

            TextRange blockRange = e.getTextRange();

            if (blockRange.getLength() > 3) {
                String s = blockRange.toString();

                if (s.indexOf("end") < 0) {
                    document.insertString(caretOffset, " end");

                }
            }
        }
        
        return super.charTyped(c, project, editor, file);
    }
}
