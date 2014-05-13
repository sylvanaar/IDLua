/*
 * Copyright 2014 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.roots.impl.ProjectFileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;

/**
 * Created by Jon on 5/12/2014.
 */
public class LuaRequireResolver implements ResolveCache.AbstractResolver<LuaReferenceElement,LuaPsiElement> {


    @Override
    public LuaPsiElement resolve(LuaReferenceElement luaReferenceElement, boolean incompleteCode) {
        final Project project = luaReferenceElement.getProject();
        final FileIndexFacade fileIndexFacade = ProjectFileIndexFacade.getInstance(project);
        final PsiManager psiManager = PsiManager.getInstance(luaReferenceElement.getProject());
        final PsiFile psiFile = luaReferenceElement.getContainingFile();
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        String path = luaReferenceElement.getName();
        if (path != null) {
            path = path.replace('.', '/').concat(".").concat(LuaFileType.DEFAULT_EXTENSION);

            final VirtualFile file = project.getBaseDir().findFileByRelativePath(path);

            if (file != null) {
                final PsiFile resolvedPsiFile = psiManager.findFile(file);

                if (resolvedPsiFile != null && resolvedPsiFile instanceof LuaPsiFile) {
                    final LuaExpression expression = ((LuaPsiFile) resolvedPsiFile).getReturnedValue().getLuaExpressions().get(0);
                    if (expression instanceof LuaReferenceElement) {
                        return (LuaPsiElement) ((LuaReferenceElement) expression).resolve();
                    }
                    return expression;
                }
            }
        }

        return null;
    }
}
