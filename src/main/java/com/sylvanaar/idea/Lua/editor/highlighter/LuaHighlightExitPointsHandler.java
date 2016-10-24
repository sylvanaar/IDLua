/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.Instruction;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author oleg
 */
public class LuaHighlightExitPointsHandler extends HighlightUsagesHandlerBase<PsiElement> {
  private final PsiElement myTarget;

  public LuaHighlightExitPointsHandler(final Editor editor, final PsiFile file, final PsiElement target) {
    super(editor, file);
    myTarget = target;
  }

  public List<PsiElement> getTargets() {
    return Collections.singletonList(myTarget);
  }

  protected void selectTargets(final List<PsiElement> targets, final Consumer<List<PsiElement>> selectionConsumer) {
    selectionConsumer.consume(targets);
  }

  public void computeUsages(final List<PsiElement> targets) {
    final PsiElement parent = myTarget.getParent();
    if (!(parent instanceof LuaReturnStatement)) {
      return;
    }

    final LuaFunctionDefinition function = PsiTreeUtil.getParentOfType(myTarget, LuaFunctionDefinition.class);
    final LuaPsiFile file = PsiTreeUtil.getParentOfType(myTarget, LuaPsiFile.class);
    if (function == null && file == null) {
      return;
    }

    highlightExitPoints((LuaReturnStatement)parent, function != null ? function.getBlock() : file);
  }

  @Nullable
  private static PsiElement getExitTarget(PsiElement exitStatement) {
    if (exitStatement instanceof LuaReturnStatement) {
      return PsiTreeUtil.getParentOfType(exitStatement, LuaFunctionDefinition.class);
    }

    return null;
  }

  private void highlightExitPoints(final LuaReturnStatement statement,
                                   final LuaBlock block) {
    final Instruction[] flow = block.getControlFlow();
    final Collection<PsiElement> exitStatements = findExitPointsAndStatements(flow);
    if (!exitStatements.contains(statement)) {
      return;
    }

    final PsiElement originalTarget = getExitTarget(statement);
    for (PsiElement exitStatement : exitStatements) {
      if (getExitTarget(exitStatement) == originalTarget) {
        addOccurrence(exitStatement);
      }
    }
    myStatusText = CodeInsightBundle.message("status.bar.exit.points.highlighted.message",
                                             exitStatements.size(),
                                             HighlightUsagesHandler.getShortcutText());
  }

  private static Collection<PsiElement> findExitPointsAndStatements(final Instruction[] flow) {
    final List<PsiElement> statements = new ArrayList<>();
    for (Instruction instruction : flow[flow.length - 1].allPred()){
      final PsiElement element = instruction.getElement();
      if (element == null){
        continue;
      }
      final PsiElement statement = PsiTreeUtil.getParentOfType(element, LuaStatementElement.class,false);
      if (statement != null){
        statements.add(statement);
      }
    }
    return statements; 
  }

  @Nullable
  @Override
  public String getFeatureId() {
    return super.getFeatureId();
  }
}
