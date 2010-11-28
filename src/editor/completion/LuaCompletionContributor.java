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


import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaVariable;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class LuaCompletionContributor extends CompletionContributor {
    private static final Logger log = Logger.getInstance("#Lua.CompletionContributor");

    private static final ElementPattern<PsiElement> AFTER_DOT = psiElement().afterLeaf(".").withParent(LuaVariable.class);

    public LuaCompletionContributor() {
        log.info("Created Lua completion contributor");

        extend(CompletionType.BASIC, psiElement(PsiElement.class), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                for (String s : LuaKeywordsManager.getKeywords())
                    result.addElement(new LuaLookupElement(s));
            }
        });

        extend(CompletionType.BASIC, AFTER_DOT, new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(@NotNull CompletionParameters parameters,
                                        ProcessingContext context,
                                        @NotNull CompletionResultSet result) {
            final PsiElement position = parameters.getPosition();

            assert position.getParent() instanceof LuaVariable;

            Object[] os = ((LuaVariable) position.getParent()).getVariants();

            for(Object o : os)
                result.addElement(new LuaLookupElement(o.toString()));
          }
        });

    }
}