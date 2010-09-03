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
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaVariable;

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

    private static final LuaManualSource LUA_MANUAL = new LuaManualSource();

  public String getQuickNavigateInfo(PsiElement element) {
//    if (element instanceof ClDef) {
//      ClDef def = (ClDef) element;
//      return def.getPresentationText();
//    }
    if (element instanceof LuaVariable) {
      LuaVariable symbol = (LuaVariable) element;
      return symbol.getText();
    }
    return null;
  }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        log.info("getUrlFor " + element);

        List<String> urls = new ArrayList<String>();

        urls.add(LUA_MANUAL.documentationUrl(element, originalElement));
        
        return urls;
    }


    /**
     * Generates the documentation for a given PsiElement. The original
     * element is the token the caret was on at the time the documentation
     * was called.
     *
     * @param element         The element for which the documentation has been requested.
     * @param originalElement The element the caret is on
     * @return The HTML formatted String which contains the documentation.
     */
    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {
        log.info("generateDoc() for " + element + " and " + originalElement);

        return LUA_MANUAL.documentationUrl(element, originalElement);
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

