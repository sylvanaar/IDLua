/*
 * Copyright 2014 Jon S Akhtar (Sylvanaar)
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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Jon on 7/25/2014.
 */
public class CodeBlockProvider implements com.intellij.codeInsight.editorActions.CodeBlockProvider {
    @Nullable
    @Override
    public TextRange getCodeBlockRange(Editor editor, PsiFile psiFile) {
        int caretOffset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(caretOffset);
        if (element == null) {
            return null;
        }
        while (caretOffset > 0 && element instanceof PsiWhiteSpace) {
            caretOffset--;
            element = psiFile.findElementAt(caretOffset);
        }

        LuaBlock block = PsiTreeUtil.getParentOfType(element, LuaBlock.class);
        final int statementStart = block.getTextRange().getStartOffset();
        int statementEnd = block.getTextRange().getEndOffset();
        while (statementEnd > statementStart && psiFile.findElementAt(statementEnd) instanceof PsiWhiteSpace) {
            statementEnd--;
        }

        if (caretOffset == statementStart || caretOffset == statementEnd) {
            final LuaBlock statementAbove = PsiTreeUtil.getParentOfType(block, LuaBlock.class);
            if (caretOffset == statementStart) {
                return new TextRange(statementAbove.getTextRange().getStartOffset(), statementEnd);
            }
            else {
                return new TextRange(statementStart, statementAbove.getTextRange().getEndOffset());
            }
        }
        if (block != null) {
            return block.getTextRange();
        }

        return null;
    }
}
