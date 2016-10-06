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

package com.sylvanaar.idea.Lua.lang.template;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/25/11
 * Time: 1:35 PM
 */
public class LuaFileMacro extends Macro {
    @Override
    public String getName() {
        return "currentLuaFile";
    }

    @Override
    public String getPresentableName() {
        return "currentLuaFile()";
    }

    @NotNull
    @Override
    public String getDefaultValue() {
        return "";
    }

    @Override
    public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext expressionContext) {
        PsiFile file = PsiDocumentManager.getInstance(
                expressionContext.getProject()).getPsiFile(expressionContext.getEditor().getDocument());

        if (file instanceof LuaPsiFile)
            return new TextResult(file.getName());

        return null;
    }

    @Override
    public Result calculateQuickResult(@NotNull Expression[] expressions, ExpressionContext expressionContext) {
        return null;
    }

    @Override
    public LookupElement[] calculateLookupItems(@NotNull Expression[] expressions, ExpressionContext expressionContext) {
        return new LookupElement[0];
    }
}
