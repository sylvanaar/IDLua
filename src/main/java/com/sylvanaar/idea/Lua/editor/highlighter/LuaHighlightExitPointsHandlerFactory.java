/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
import org.jetbrains.annotations.NotNull;

/**
 * @author oleg
 */
public class LuaHighlightExitPointsHandlerFactory extends HighlightUsagesHandlerFactoryBase {
  public HighlightUsagesHandlerBase createHighlightUsagesHandler(@NotNull Editor editor, @NotNull PsiFile file, @NotNull PsiElement target) {
    final LuaReturnStatement returnStatement = PsiTreeUtil.getParentOfType(target, LuaReturnStatement.class);
    if (returnStatement != null) {
      final LuaExpressionList returnExpr = returnStatement.getReturnValue();
      if (returnExpr == null || !PsiTreeUtil.isAncestor(returnExpr, target, false)) {
        return new LuaHighlightExitPointsHandler(editor, file, target);
      }
    }
    return null;
  }
}
