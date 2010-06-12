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

package com.sylvanaar.idea.Lua.editor.inspections.quickfixes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 7:28:35 AM
 */
public abstract class AbstractQuickfix implements LocalQuickFix, IntentionAction {
    @NotNull
    public final String getText() {
        return getName();
    }

    @NotNull
    public String getFamilyName() {
        return "Lua";
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file instanceof LuaPsiFile;
    }

    public boolean startInWriteAction() {
        return true;
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        invoke(project, null, descriptor.getPsiElement().getContainingFile());
    }
}
