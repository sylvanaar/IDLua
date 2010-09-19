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

import com.intellij.lang.documentation.QuickDocumentationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 3:13:21 AM
 */
public class LuaDocumentationProvider extends QuickDocumentationProvider {
    private static final Logger log = Logger.getInstance("#Lua.LuaDocumentationProvider");

       private static final List<DocumentationSource> sourceList = new ArrayList<DocumentationSource>(0);

    static {
        sourceList.add(new LuaManualSource());
        sourceList.add(new StandardFunctionDocumentation());
//        sourceList.add(new InternalCommandDocumentation());
//        sourceList.add(new CachingDocumentationSource(new SystemInfopageDocSource()));
//        sourceList.add(new ManpageDocSource());
    }

  public String getQuickNavigateInfo(PsiElement element) {

    return documentation(element, element);
  }

    /**
     * Returns the documentation for the given element and the originalElement.
     * It iterates through the list of documentation sources and returns the first
     * hit.
     *
     * @param element         The element for which the documentation is requested.
     * @param originalElement The element the caret was on.
     * @return The HTML formatted documentation string.
     */
    static String documentation(PsiElement element, PsiElement originalElement) {
        log.info("documentation for " + element);
        for (DocumentationSource source : sourceList) {
            log.info("Trying with " + source);
            String doc = source.documentation(element, originalElement);
            if (doc != null) {
                return doc;
            }
        }

        return "No documentation found.";
    }

    /**
     * Returns an external link to a command.
     *
     * @param element         The element for which the documentation is requested.
     * @param originalElement The element the caret was on.
     * @return The url which leads to the online documentation.
     */
    static String documentationUrl(PsiElement element, PsiElement originalElement) {
        log.info("documentationUrl for " + element);
        for (DocumentationSource source : sourceList) {
            String url = source.documentationUrl(element, originalElement);
            if (url != null) {
                return url;
            }
        }

        return null;
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        log.info("getDocumentationElementForLookupItem: element: " + element);
        return element;
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        log.info("getDocumentationElementForLink: element: " + context);
        return context;
    }
}

