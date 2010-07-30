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

package com.sylvanaar.idea.Lua.editor.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;

import java.util.*;

public class LuaCompletionContributor extends CompletionContributor {
    private static final Logger log = Logger.getInstance("#Lua.CompletionContributor");
    private List<LuaLookupElement> allLookupElements = new ArrayList<LuaLookupElement>();
    private Map<String, Set<LuaLookupElement>> contextToDirectiveNameElements = new HashMap<String, Set<LuaLookupElement>>();
    private List<LuaLookupElement> mainContextDirectiveNameElements = new ArrayList<LuaLookupElement>();

    private static List<LuaLookupElement> booleanVariants = new ArrayList<LuaLookupElement>();
    {
        booleanVariants.add(new LuaLookupElement("true"));
        booleanVariants.add(new LuaLookupElement("false"));
    }

    private LuaKeywordsManager keywords;

    public LuaCompletionContributor(LuaKeywordsManager keywords) {
        log.info("Created Lua completion contributor");
            
        this.keywords = keywords;

        for (String keyword : keywords.getKeywords()) {
            allLookupElements.add(new LuaLookupElement(keyword));
        }

    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        if (parameters.getOriginalFile() instanceof LuaPsiFile) {

            PsiElement parent = parameters.getPosition().getParent();

            log.info("fill completion " + parameters + result);

//            if (parent instanceof LuaIdentifier) {
//                suggestName(result, parent);
//
//            } else if (parent instanceof LuaDirectiveValue) {
//
//                PsiElement variable = parent.getPrevSibling();
//                if (variable != null) {
//
//                    if (variable instanceof LuaInnerVariable) {
//                        result = result.withPrefixMatcher(((LuaInnerVariable)variable).getName());
//                    } else if (variable instanceof LuaDirectiveValue) {
//                        if (variable.getText().endsWith("$")) {
//                            //this is ctrl+space at the end of asdqwe$ (asdqwe$ is treated as simple value)
//                            result = result.withPrefixMatcher("");
//                        }
//                    } else {
//                        throw new AssertionError("got some weird type when autocompleting"); //hmm...
//                    }
//                    suggestVariable(result);
//
//                } else {
//                    suggestValue(result, (LuaDirectiveValue) parent);
//                }
//            }


  //          suggestName(result, parent);
        }

    }

    private void suggestName(CompletionResultSet result, LuaNamedElement where) {

    }

    private void suggestValue(CompletionResultSet result, LuaNamedElement where) {

    }

    private void suggestVariable(CompletionResultSet result) {

    }


}