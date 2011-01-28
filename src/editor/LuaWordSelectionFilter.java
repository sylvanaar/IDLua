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
package com.sylvanaar.idea.Lua.editor;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

import static com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes.*;


/**
 * @author Maxim.Medvedev
 */
public class LuaWordSelectionFilter implements Condition<PsiElement> {
  public boolean value(PsiElement element) {

    final ASTNode node = element.getNode();
    if (node == null) return false;

    final IElementType type = node.getElementType();
    if (type == NAME||
        type == STRING ||
        type == LONGSTRING||
        type == SHORTCOMMENT||
        type == LONGCOMMENT||
        type == LONGCOMMENT_END ||
        type == LONGCOMMENT_BEGIN||
        type == LONGSTRING_END ||
        type == LONGSTRING_BEGIN) {
      return true;
    }

      return true;

//    return !(element instanceof LuaBlock) &&
//           !(element instanceof LuaExpressionList) &&
//           !(element instanceof LuaParameterList) &&
//           !(element instanceof LuaIdentifierList)
//
//      ;
  }
}
