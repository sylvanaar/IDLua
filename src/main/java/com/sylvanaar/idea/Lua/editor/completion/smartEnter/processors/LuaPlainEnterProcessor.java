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
package com.sylvanaar.idea.Lua.editor.completion.smartEnter.processors;

import com.intellij.lang.SmartEnterProcessorWithFixers;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaConditionalLoop;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaIfThenStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaWhileStatement;
import org.jetbrains.annotations.NotNull;


public class LuaPlainEnterProcessor extends SmartEnterProcessorWithFixers.FixEnterProcessor {
  private LuaBlock getControlStatementBlock(int caret, PsiElement element) {
    LuaBlock body = null;
    if (element instanceof LuaIfThenStatement) {
      body = ((LuaIfThenStatement) element).getIfBlock();
      if (caret > body.getTextRange().getEndOffset()) {
        body = ((LuaIfThenStatement) element).getElseBlock();
      }
    } else if (element instanceof LuaWhileStatement) {
      body = ((LuaWhileStatement) element).getBlock();
    } else if (element instanceof LuaConditionalLoop) {
      body = ((LuaConditionalLoop) element).getBlock();
    }

    return body;
  }

  @Override
  public boolean doEnter(PsiElement psiElement, PsiFile file, @NotNull Editor editor, boolean modified) {
    LuaBlock block = getControlStatementBlock(editor.getCaretModel().getOffset(), psiElement);

    if (block != null) {
      PsiElement firstElement = block.getFirstChild().getNextSibling();

      final int offset = firstElement != null ? firstElement.getTextRange().getStartOffset() - 1 : block.getTextRange().getEndOffset();
      editor.getCaretModel().moveToOffset(offset);
    }

    plainEnter(editor);
    return true;
  }
}
