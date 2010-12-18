/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.sylvanaar.idea.Lua.intentions.base;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.intentions.utils.QuickfixUtil;
import com.sylvanaar.idea.Lua.intentions.LuaIntentionsBundle;
import com.sylvanaar.idea.Lua.intentions.utils.BoolUtils;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElementFactory;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



public abstract class Intention implements IntentionAction {
  private final PsiElementPredicate predicate;

  /**
   * @noinspection AbstractMethodCallInConstructor,OverridableMethodCallInConstructor
   */
  protected Intention() {
    super();
    predicate = getElementPredicate();
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file)
      throws IncorrectOperationException {
    if (!QuickfixUtil.ensureFileWritable(project, file)) {
      return;
    }
    final PsiElement element = findMatchingElement(file, editor);
    if (element == null) {
      return;
    }
    assert element.isValid() : element;
    processIntention(element);
  }

  protected abstract void processIntention(@NotNull PsiElement element)
      throws IncorrectOperationException;

  @NotNull
  protected abstract PsiElementPredicate getElementPredicate();


  protected static void replaceExpressionWithNegatedExpressionString(
      @NotNull String newExpression,
      @NotNull LuaExpression expression)
      throws IncorrectOperationException {
    final LuaPsiElementFactory factory = LuaPsiElementFactory.getInstance(expression.getProject());

    LuaExpression expressionToReplace = expression;
    final String expString;
    if (BoolUtils.isNegated(expression)) {
      expressionToReplace = BoolUtils.findNegation(expression);
      expString = newExpression;
    } else {
      expString = "not (" + newExpression + ')';
    }
    final LuaExpression newCall =
        factory.createExpressionFromText(expString);
    assert expressionToReplace != null;
    expressionToReplace.replaceWithExpression(newCall, true);
  }


  @Nullable
  PsiElement findMatchingElement(PsiFile file,
                                 Editor editor) {
    final CaretModel caretModel = editor.getCaretModel();
    final int position = caretModel.getOffset();
    PsiElement element = file.findElementAt(position);
    while (element != null) {
      if (predicate.satisfiedBy(element)) {
        return element;
      } else {
        element = element.getParent();
        if (isStopElement(element)) {
          break;
        }
      }
    }
    return null;
  }

  protected boolean isStopElement(PsiElement element) {
    return element instanceof PsiFile;
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return findMatchingElement(file, editor) != null;
  }

  public boolean startInWriteAction() {
    return true;
  }

  private String getPrefix() {
    final Class<? extends Intention> aClass = getClass();
    final String name = aClass.getSimpleName();
    final StringBuilder buffer = new StringBuilder(name.length() + 10);
    buffer.append(Character.toLowerCase(name.charAt(0)));
    for (int i = 1; i < name.length(); i++) {
      final char c = name.charAt(i);
      if (Character.isUpperCase(c)) {
        buffer.append('.');
        buffer.append(Character.toLowerCase(c));
      } else {
        buffer.append(c);
      }
    }
    return buffer.toString();
  }

  @NotNull
  public String getText() {
    return LuaIntentionsBundle.message(getPrefix() + ".name");
  }

  @NotNull
  public String getFamilyName() {
    return LuaIntentionsBundle.message(getPrefix() + ".family.name");
  }
}
