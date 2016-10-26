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

package com.sylvanaar.idea.Lua.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 5/15/11
 * Time: 5:07 AM
 */
public class LuaDebuggerEvaluator extends XDebuggerEvaluator {
    private static final Logger log = Logger.getInstance("Lua.LuaDebuggerEvaluator");

    private LuaDebuggerController myController;

    public LuaDebuggerEvaluator(LuaDebuggerController myController) {
        this.myController = myController;
    }

    @Override
    public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback,
                         @Nullable XSourcePosition expressionPosition) {
        log.debug("evaluating: " + expression);
        Promise<LuaDebugValue> promise = myController.execute("return " + expression);
        promise.done(callback::evaluated);
    }

    @Override
    public boolean isCodeFragmentEvaluationSupported() {
        return false;
    }

    @Nullable
    @Override
    public TextRange getExpressionRangeAtOffset(final Project project, final Document document, final int offset, final boolean sideEffectsAllowed) {
        final Ref<TextRange> currentRange = Ref.create(null);
        PsiDocumentManager.getInstance(project).commitAndRunReadAction(() -> {
            try {
                PsiElement elementAtCursor = XDebuggerUtil.getInstance().findContextElement(PsiDocumentManager
                        .getInstance(project).getPsiFile(document).getVirtualFile(), offset, project, false);
                if (elementAtCursor == null) return;
                Pair<PsiElement, TextRange> pair = findExpression(elementAtCursor, sideEffectsAllowed);
                if (pair != null) {
                    currentRange.set(pair.getSecond());
                }
            } catch (IndexNotReadyException ignored) {}
        });
        return currentRange.get();
    }

    @Nullable
    private static Pair<PsiElement, TextRange> findExpression(PsiElement element, boolean allowMethodCalls) {
        LuaExpression expression = PsiTreeUtil.getParentOfType(element, LuaExpression.class);

        if (expression == null) return null;


        return Pair.create(expression, expression.getTextRange());
    }
}
