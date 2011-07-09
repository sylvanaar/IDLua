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

package com.sylvanaar.idea.Lua.refactoring;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.lexer.Lexer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.ReflectionCache;
import com.sylvanaar.idea.Lua.lang.lexer.LuaLexer;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFileBase;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 7/9/11
 * Time: 6:56 AM
 */
public class LuaRefactoringUtil {
  @Nullable
  public static PsiElement getEnclosingContainer(PsiElement place) {
    PsiElement parent = place.getParent();
    while (true) {
      if (parent == null) {
        return null;
      }
      if (parent instanceof LuaBlock) {
        return parent;
      }
      parent = parent.getParent();
    }
  }

  public static void sortOccurrences(PsiElement[] occurences) {
    Arrays.sort(occurences, new Comparator<PsiElement>() {
        public int compare(PsiElement elem1, PsiElement elem2) {
            final int offset1 = elem1.getTextRange().getStartOffset();
            final int offset2 = elem2.getTextRange().getStartOffset();
            return offset1 - offset2;
        }
    });
  }

  @Nullable
  public static <T extends PsiElement> T findElementInRange(final LuaPsiFileBase file,
                                                            int startOffset,
                                                            int endOffset,
                                                            final Class<T> klass) {
    PsiElement element1 = file.getViewProvider().findElementAt(startOffset, file.getLanguage());
    PsiElement element2 = file.getViewProvider().findElementAt(endOffset - 1, file.getLanguage());
    if (LuaTokenTypes.WHITE_SPACES_SET.contains(element1.getNode().getElementType())) {
      startOffset = element1.getTextRange().getEndOffset();
      element1 = file.getViewProvider().findElementAt(startOffset, file.getLanguage());
    }
    if (LuaTokenTypes.WHITE_SPACES_SET.contains(element2.getNode().getElementType())) {
      endOffset = element2.getTextRange().getStartOffset();
      element2 = file.getViewProvider().findElementAt(endOffset - 1, file.getLanguage());
    }
    if (element2 == null || element1 == null) return null;
    final PsiElement commonParent = PsiTreeUtil.findCommonParent(element1, element2);
    assert commonParent != null;
    final T element = ReflectionCache.isAssignable(klass, commonParent.getClass())
        ? (T) commonParent : PsiTreeUtil.getParentOfType(commonParent, klass);
    if (element == null || element.getTextRange().getStartOffset() != startOffset) {
      return null;
    }
    return element;
  }

  public static PsiElement[] getExpressionOccurrences(@NotNull PsiElement expr, @NotNull PsiElement scope) {
    ArrayList<PsiElement> occurrences = new ArrayList<PsiElement>();
    Comparator<PsiElement> comparator = new Comparator<PsiElement>() {
      public int compare(PsiElement element1, PsiElement element2) {
        if (element1.equals(element2)) return 0;

        if (element1 instanceof LuaParameter &&
            element2 instanceof LuaParameter) {
          final String name1 = ((LuaParameter) element1).getName();
          final String name2 = ((LuaParameter) element2).getName();
          if (name1 != null && name2 != null) {
            return name1.compareTo(name2);
          }
        }
        return 1;
      }
    };

//    if (scope instanceof LuaConditionalLoop) {
//      PsiElement son = expr;
//      while (son.getParent() != null && !(son.getParent() instanceof GrLoopStatement)) {
//        son = son.getParent();
//      }
//      assert scope.equals(son.getParent());
//      collectOccurrences(expr, son, occurrences, comparator, false);
//    } else {
      collectOccurrences(expr, scope, occurrences, comparator,  scope instanceof LuaPsiFileBase);
//    }
    return PsiUtilBase.toPsiElementArray(occurrences);
  }

  private static void collectOccurrences(@NotNull PsiElement expr, @NotNull PsiElement scope, @NotNull ArrayList<PsiElement> acc, Comparator<PsiElement> comparator, boolean goIntoInner) {
    if (scope.equals(expr)) {
      acc.add(expr);
      return;
    }
    for (PsiElement child : scope.getChildren()) {
//      if (goIntoInner || !(scope instanceof LuaPsiFileBase)) {
        if (PsiEquivalenceUtil.areElementsEquivalent(child, expr, comparator, false)) {
          acc.add(child);
        } else {
          collectOccurrences(expr, child, acc, comparator, goIntoInner);
        }
      }
//    }
  }

    public static boolean isAppropriateContainerForIntroduceVariable(PsiElement realContainer) {
        return realContainer instanceof LuaBlock;
    }

    public static boolean isIdentifier(String text) {
        if (text == null) return false;

        Lexer lexer = new LuaLexer();
        lexer.start(text);
        if (lexer.getTokenType() != LuaTokenTypes.NAME) return false;
        lexer.advance();
        return lexer.getTokenType() == null;
    }
}
