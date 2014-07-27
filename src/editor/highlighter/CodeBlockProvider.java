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
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import org.jetbrains.annotations.Nullable;

public class CodeBlockProvider implements com.intellij.codeInsight.editorActions.CodeBlockProvider {
    @Nullable
    @Override
    public TextRange getCodeBlockRange(Editor editor, PsiFile psiFile) {
        int caretOffset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(caretOffset);
        if (element == null) {
            return null;
        }

        if (LuaTokenTypes.BLOCK_CLOSE_SET.contains(element.getNode().getElementType()) && caretOffset == element.getTextOffset())
            element = psiFile.findElementAt(element.getTextOffset()-1);

        LuaBlock block = PsiTreeUtil.getParentOfType(element, LuaBlock.class);

        if (block != null) {
            LuaBlock outerBlock = PsiTreeUtil.getParentOfType(block, LuaBlock.class);
            if (outerBlock instanceof PsiFile)
                outerBlock = null;
            final int innerBlockEnd = block.getCloseElement().getTextRange().getStartOffset();
            final int innerBlockStart = block.getOpenElement().getTextRange().getEndOffset();
            if (outerBlock != null && caretOffset == innerBlockStart) {
                return new TextRange(outerBlock.getOpenElement().getTextRange().getEndOffset(),
                        innerBlockEnd);
            } else if (outerBlock != null && caretOffset == innerBlockEnd) {
                return new TextRange( innerBlockStart, outerBlock.getCloseElement().getTextRange().getStartOffset());
            } else {
                return new TextRange(innerBlockStart, innerBlockEnd);
            }
        }

        return null;
    }
}
