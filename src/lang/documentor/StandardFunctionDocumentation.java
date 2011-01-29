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

package com.sylvanaar.idea.Lua.lang.documentor;


import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.util.PathUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;

import java.util.ArrayList;
import java.util.List;


public class StandardFunctionDocumentation implements DocumentationProvider {
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
       return MethodSignatureBundle.message(element.getText());
    }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        List<String> rc = new ArrayList<String>();
        rc.add(VfsUtil.pathToUrl(PathUtil.getJarPathForClass(LuaPsiFile.class)) +
            "/#pdf-" + element.getText() + ".html");

        return rc;
    }

    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {
        return MethodSignatureBundle.message(element.getText());
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
