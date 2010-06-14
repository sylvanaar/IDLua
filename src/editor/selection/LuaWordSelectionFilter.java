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

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameterList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlockStatement;

import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.*;


/**
 * @author Maxim.Medvedev
 */
public class LuaWordSelectionFilter implements Condition<PsiElement> {
  public boolean value(PsiElement element) {

    final ASTNode node = element.getNode();
    if (node == null) return false;

    final IElementType type = node.getElementType();
    if (type == NAME ||
        type == LONGSTRING ||
        type == STRING ) {
      return true;
    }

    return !(element instanceof LuaBlockStatement) &&
           !(element instanceof LuaParameterList);

  }
}