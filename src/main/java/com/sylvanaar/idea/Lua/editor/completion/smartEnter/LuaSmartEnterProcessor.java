/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.sylvanaar.idea.Lua.editor.completion.smartEnter;

import com.intellij.lang.SmartEnterProcessorWithFixers;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.text.CharArrayUtil;
import com.sylvanaar.idea.Lua.editor.completion.smartEnter.fixers.LuaBlockFixer;
import com.sylvanaar.idea.Lua.editor.completion.smartEnter.fixers.LuaIfConditionFixer;
import com.sylvanaar.idea.Lua.editor.completion.smartEnter.fixers.LuaWhileConditionFixer;
import com.sylvanaar.idea.Lua.editor.completion.smartEnter.processors.LuaPlainEnterProcessor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaIfThenStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaWhileStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.patterns.PsiJavaPatterns.psiElement;


public class LuaSmartEnterProcessor extends SmartEnterProcessorWithFixers {
  public LuaSmartEnterProcessor() {
    final List<SmartEnterProcessorWithFixers.Fixer<LuaSmartEnterProcessor>> ourFixers = Arrays.asList(
            new LuaIfConditionFixer(),
            new LuaBlockFixer(),
            new LuaWhileConditionFixer()
    );
    addFixers(ourFixers.toArray(new Fixer[ourFixers.size()]));
    addEnterProcessors(new LuaPlainEnterProcessor());
  }

  @Override
  protected void reformat(PsiElement atCaret) throws IncorrectOperationException {
    PsiElement parent = atCaret.getParent();
    if (parent instanceof LuaBlock) {
      final LuaBlock block = (LuaBlock) parent;
      if (block.getStatements().length > 0 && block.getStatements()[0] == atCaret) {
        atCaret = block;
      }
    }

    if (atCaret instanceof LuaIfThenStatement) {
      if (((LuaIfThenStatement) atCaret).getIfCondition().getTextLength() == 0)
        return;
    }
    if (atCaret instanceof LuaWhileStatement) {
      if (((LuaWhileStatement) atCaret).getCondition().getTextLength() == 0)
        return;
    }
    super.reformat(atCaret);
  }

  @Override
  public boolean doNotStepInto(PsiElement element) {
    return element instanceof LuaBlock;
  }

  @Override
  @Nullable
  protected PsiElement getStatementAtCaret(Editor editor, PsiFile psiFile) {
    PsiElement atCaret = super.getStatementAtCaret(editor, psiFile);

    if (atCaret instanceof PsiWhiteSpace) return null;

    PsiElement statementAtCaret = PsiTreeUtil.getParentOfType(atCaret,
            LuaStatementElement.class
    );

    return statementAtCaret instanceof LuaStatementElement
            ? statementAtCaret
            : null;
  }

  @Override
  protected void moveCaretInsideBracesIfAny(@NotNull final Editor editor, @NotNull final PsiFile file) throws IncorrectOperationException {
    int caretOffset = editor.getCaretModel().getOffset();
    final CharSequence chars = editor.getDocument().getCharsSequence();

    if (CharArrayUtil.regionMatches(chars, caretOffset, "end")) {
      caretOffset += 3;
    } else if (CharArrayUtil.regionMatches(chars, caretOffset, "\nend")) {
      caretOffset += 4;
    }

//    caretOffset = CharArrayUtil.shiftBackward(chars, caretOffset - 3, " \t") + 1;

    if (CharArrayUtil.regionMatches(chars, caretOffset - "end".length(), "end") ||
            CharArrayUtil.regionMatches(chars, caretOffset - "\nend".length(), "\nend")) {
      commit(editor);
      final CodeStyleSettings settings = CodeStyleSettingsManager.getSettings(file.getProject());
      final boolean old = settings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE;
      settings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE = false;
      PsiElement elt = PsiTreeUtil.getParentOfType(file.findElementAt(caretOffset - 4), LuaBlock.class, false);
      reformat(elt);
      settings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE = old;
      editor.getCaretModel().moveToOffset(caretOffset - 4);
    }
  }

  private static PsiElement[] getChildren(PsiElement element) {
    PsiElement psiChild = element.getFirstChild();
    if (psiChild == null) return PsiElement.EMPTY_ARRAY;

    List<PsiElement> result = new ArrayList<>();
    while (psiChild != null) {
      result.add(psiChild);

      psiChild = psiChild.getNextSibling();
    }
    return PsiUtilCore.toPsiElementArray(result);
  }
}
