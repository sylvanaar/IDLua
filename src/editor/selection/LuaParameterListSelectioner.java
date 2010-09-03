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
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameterList;

import java.util.Collections;
import java.util.List;

/**
 * @author Maxim.Medvedev
 */
public class LuaParameterListSelectioner extends LuaBasicSelectioner {
  @Override
  public boolean canSelect(PsiElement e) {
    return e instanceof LuaParameterList;
  }

  @Override
  public List<TextRange> select(PsiElement e, CharSequence editorText, int cursorOffset, Editor editor) {
    if (e.getParent() instanceof LuaParameterList) e = e.getParent();
    if (!(e instanceof LuaParameterList)) return Collections.emptyList();
    return Collections.singletonList(e.getTextRange());
  }
}