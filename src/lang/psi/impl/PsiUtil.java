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

package com.sylvanaar.idea.Lua.lang.psi.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParenthesizedExpression;
import org.jetbrains.annotations.Nullable;

/**
 * @author ven
 */
public class PsiUtil {
  public static final Logger LOG = Logger.getInstance("Lua.PsiUtil");

  private PsiUtil() {
  }

    
    public static boolean hasErrorElementChild(PsiElement element) {
      for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
        if (child instanceof PsiErrorElement) return true;
      }
      return false;
    }

  public static void reformatCode(final PsiElement element) {
    final TextRange textRange = element.getTextRange();
    try {
      CodeStyleManager.getInstance(element.getProject())
        .reformatText(element.getContainingFile(), textRange.getStartOffset(), textRange.getEndOffset());
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  @Nullable
  public static PsiElement getPrevNonSpace(final PsiElement elem) {
    PsiElement prevSibling = elem.getPrevSibling();
    while (prevSibling instanceof PsiWhiteSpace) {
      prevSibling = prevSibling.getPrevSibling();
    }
    return prevSibling;
  }

  @Nullable
  public static PsiElement skipParentheses(@Nullable PsiElement element, boolean up) {
    if (element == null) return null;
    if (up) {
      PsiElement parent;
      while ((parent=element.getParent()) instanceof LuaParenthesizedExpression) {
        element = parent;
      }
      return element;
    }
    else {
      while (element instanceof LuaParenthesizedExpression) {
        element = ((LuaParenthesizedExpression)element).getOperand();
      }
      return element;
    }
  }
}
