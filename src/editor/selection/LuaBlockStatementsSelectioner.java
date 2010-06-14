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

package com.sylvanaar.idea.Lua.editor.selection;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementList;

import java.util.List;

/**
 * @author ilyas
 */
public class LuaBlockStatementsSelectioner extends LuaBasicSelectioner {

  public boolean canSelect(PsiElement e) {
    return e instanceof LuaStatementList;
  }

  public List<TextRange> select(PsiElement e, CharSequence editorText, int cursorOffset, Editor editor) {
    List<TextRange> result = super.select(e, editorText, cursorOffset, editor);

    if (e instanceof LuaStatementList) {
      LuaStatementList block = ((LuaStatementList) e);
      PsiStatement[] statements = block.getStatements();

      if (statements.length > 0) {
        int startOffset = statements[0].getTextRange().getStartOffset();
        int endOffset = statements[statements.length - 1].getTextRange().getEndOffset();
        TextRange range = new TextRange(startOffset, endOffset);
        result.add(range);
      }
    }
    return result;
  }

}