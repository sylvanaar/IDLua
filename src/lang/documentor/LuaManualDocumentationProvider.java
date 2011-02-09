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
import com.intellij.lang.documentation.ExternalDocumentationProvider;
import com.intellij.lang.java.JavaDocumentationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 3:30:10 AM
 */
public class LuaManualDocumentationProvider implements DocumentationProvider, ExternalDocumentationProvider {
    private final static String LUA_ORG_DOCUMENTATION_URL = "http://www.lua.org/manual/5.1/manual.html";

    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        List<String> rc = new ArrayList<String>();
            rc.add(LUA_ORG_DOCUMENTATION_URL +"#pdf-" + element.getText() );

        return rc;
    }

    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String fetchExternalDocumentation(Project project, PsiElement element, List<String> docUrls) {
        return JavaDocumentationProvider.fetchExternalJavadoc(element, project, docUrls);
    }
}
