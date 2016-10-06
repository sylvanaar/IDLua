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

package com.sylvanaar.idea.Lua.editor.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 7:28:35 AM
 */
public abstract class LuaFix implements LocalQuickFix {
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

    public void applyFix(@NotNull Project project,
                         @NotNull ProblemDescriptor descriptor) {
      final PsiElement problemElement = descriptor.getPsiElement();
      if (problemElement == null || !problemElement.isValid()) {
        return;
      }
      if (isQuickFixOnReadOnlyFile(problemElement)) {
        return;
      }
      try {
        doFix(project, descriptor);
      } catch (IncorrectOperationException e) {
        final Class<? extends LuaFix> aClass = getClass();
        final String className = aClass.getName();
        final Logger logger = Logger.getInstance(className);
        logger.error(e);
      }
    }

    protected abstract void doFix(Project project, ProblemDescriptor descriptor)
        throws IncorrectOperationException;

    private static boolean isQuickFixOnReadOnlyFile(PsiElement problemElement) {
      final PsiFile containingPsiFile = problemElement.getContainingFile();
      if (containingPsiFile == null) {
        return false;
      }
      final VirtualFile virtualFile = containingPsiFile.getVirtualFile();
     
      final Project project = problemElement.getProject();
      final ReadonlyStatusHandler handler = ReadonlyStatusHandler.getInstance(project);
        final ReadonlyStatusHandler.OperationStatus status;
        if (virtualFile != null) {
            status = handler.ensureFilesWritable(virtualFile);
            return status.hasReadonlyFiles();
        }

        return false;
    }



}
