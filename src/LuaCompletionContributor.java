/*
 * Copyright 2009 Max Ishchenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 15.07.2009
 * Time: 0:37:39
 */
public class LuaCompletionContributor extends CompletionContributor {

    private List<LuaLookupElement> allLookupElements = new ArrayList<LuaLookupElement>();
    private Map<String, Set<LuaLookupElement>> contextToDirectiveNameElements = new HashMap<String, Set<LuaLookupElement>>();
    private List<LuaLookupElement> mainContextDirectiveNameElements = new ArrayList<LuaLookupElement>();

    private static List<LuaLookupElement> booleanVariants = new ArrayList<LuaLookupElement>();
    {
        booleanVariants.add(new LuaLookupElement("on"));
        booleanVariants.add(new LuaLookupElement("off"));
    }

    private LuaKeywordsManager keywords;

    public LuaCompletionContributor(LuaKeywordsManager keywords) {

        this.keywords = keywords;

        for (String keyword : keywords.getKeywords()) {
            allLookupElements.add(new LuaLookupElement(keyword));
        }

        Map<String, Set<String>> contextToDirectives = keywords.getContextToDirectiveListMappings();
        for (Map.Entry<String, Set<String>> entry : contextToDirectives.entrySet()) {
            Set<LuaLookupElement> directives = new HashSet<LuaLookupElement>();
            for (String directive : entry.getValue()) {
                directives.add(new LuaLookupElement(directive));
            }
            contextToDirectiveNameElements.put(entry.getKey(), directives);
        }

        for (String directive : keywords.getDirectivesThatCanResideInMainContext()) {
            mainContextDirectiveNameElements.add(new LuaLookupElement(directive));
        }

    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {

        if (parameters.getOriginalFile() instanceof LuaPsiFile) {

            PsiElement parent = parameters.getPosition().getParent();

            if (parent instanceof LuaDirectiveName && !((LuaDirectiveName) parent).getDirective().isInChaosContext()) {
                suggestName(result, (LuaDirectiveName) parent);

            } else if (parent instanceof LuaDirectiveValue) {

                PsiElement variable = parent.getPrevSibling();
                if (variable != null) {

                    if (variable instanceof LuaInnerVariable) {
                        result = result.withPrefixMatcher(((LuaInnerVariable)variable).getName()); 
                    } else if (variable instanceof LuaDirectiveValue) {
                        if (variable.getText().endsWith("$")) {
                            //this is ctrl+space at the end of asdqwe$ (asdqwe$ is treated as simple value)
                            result = result.withPrefixMatcher("");
                        }
                    } else {
                        throw new AssertionError("got some weird type when autocompleting"); //hmm...
                    }
                    suggestVariable(result);

                } else {
                    suggestValue(result, (LuaDirectiveValue) parent);
                }
            }

        }

    }

    private void suggestName(CompletionResultSet result, LuaDirectiveName where) {

        LuaContext context = where.getDirective().getParentContext();
        if (context == null) {
            for (LuaLookupElement mainContextElement : mainContextDirectiveNameElements) {
                result.addElement(mainContextElement);
            }
        } else {
            String contextName = context.getDirective().getNameString();
            Set<LuaLookupElement> elementsForContext = contextToDirectiveNameElements.get(contextName);
            if (elementsForContext == null) {
                //parent directive might be unknown. suggest all.
                for (LuaLookupElement LuaLookupElement : allLookupElements) {
                    result.addElement(LuaLookupElement);
                }
            } else {
                for (LuaLookupElement LuaLookupElement : elementsForContext) {
                    result.addElement(LuaLookupElement);
                }
            }
        }

    }

    private void suggestValue(CompletionResultSet result, LuaDirectiveValue where) {
        if (keywords.checkBooleanKeyword(where.getDirective().getNameString())) {
            for (LuaLookupElement booleanVariant : booleanVariants) {
                result.addElement(booleanVariant);
            }
        }
    }

    private void suggestVariable(CompletionResultSet result) {

        for (String variable : keywords.getVariables()) {
            result.addElement(new LuaLookupElement(variable));
        }

    }

    public static class LuaLookupElement extends LookupElement {

        private String str;

        public LuaLookupElement(String str) {
            this.str = str;
        }

        public InsertHandler<? extends LookupElement> getInsertHandler() {
            return null;
        }

        @NotNull
        public String getLookupString() {
            return str;
        }

        @NotNull
        protected LookupElementRenderer<? extends LookupElement> getRenderer() {
            return new SimpleLookupElementRenderer();
        }

    }

    private static class SimpleLookupElementRenderer extends LookupElementRenderer {
        public void renderElement(LookupElement element, LookupElementPresentation presentation) {
            presentation.setItemText(element.getLookupString());
        }
    }


}
