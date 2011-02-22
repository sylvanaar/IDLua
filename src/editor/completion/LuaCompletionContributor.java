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
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.impl.statements.LuaFunctionDefinitionStatementImpl;
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.LuaGlobalDeclarationIndex;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class LuaCompletionContributor extends DefaultCompletionContributor {
    private static final Logger log = Logger.getInstance("#Lua.CompletionContributor");

    private static final ElementPattern<PsiElement> AFTER_SELF_DOT = psiElement().withParent(LuaCompoundIdentifier.class).afterSibling(psiElement().withName("self"));
    private static final ElementPattern<PsiElement> AFTER_DOT = psiElement().withParent(LuaIdentifier.class).afterLeaf(".", ":");

    private static final ElementPattern<PsiElement> NOT_AFTER_DOT = psiElement().withParent(LuaIdentifier.class).andNot(psiElement().afterLeaf(".", ":"));
    private static final ElementPattern<PsiElement> ANY_ID = psiElement().withParent(LuaIdentifier.class);


    private static final ElementPattern<PsiElement> AFTER_SELF =
            psiElement().withParent(LuaSymbol.class).afterLeaf(":",".");

    public LuaCompletionContributor() {
        extend(CompletionType.BASIC, NOT_AFTER_DOT, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                for (String s : LuaKeywordsManager.getKeywords())
                    result.addElement(new LuaLookupElement(s));
            }
        });

//        extend(CompletionType.BASIC, AFTER_DOT, new CompletionProvider<CompletionParameters>() {
//            @Override
//            protected void addCompletions(@NotNull CompletionParameters parameters,
//                                          ProcessingContext context,
//                                          @NotNull CompletionResultSet result) {
//
//                fieldVisitor.reset();
//
//                ((LuaPsiFile)parameters.getOriginalFile()).accept(fieldVisitor);
//
//                for (String s : fieldVisitor.getResult()) {
//                    result.addElement(new LuaLookupElement(s));
//                    result.addElement(new LuaLookupElement("self:"+s));
//                }
//            }
//        });

        extend(CompletionType.BASIC, AFTER_SELF, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters,
                                          ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement element = parameters.getPosition();
                while (!(element instanceof LuaFunctionDefinitionStatementImpl) && element != null)
                    element = element.getContext();

                // Must be inside a function
                if (element == null) return;

                LuaFunctionDefinitionStatementImpl func = (LuaFunctionDefinitionStatementImpl) element;

                LuaSymbol symbol = func.getIdentifier();

                int colonIdx = symbol.getText().lastIndexOf(':');
                int dotIdx = symbol.getText().lastIndexOf('.');
                if (colonIdx < 0 && dotIdx < 0) return;

                int idx = Math.max(colonIdx, dotIdx);

                String prefix = symbol.getText().substring(0, idx+1);

                for(String key : LuaGlobalDeclarationIndex.getInstance().getAllKeys(element.getProject())) {
                    System.out.println(key);

                    if (key.startsWith(prefix))
                        result.addElement(new LuaLookupElement("self:"+key.substring(prefix.length())));
                }
            }
        });
    }


    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);    //To change body of overridden methods use File | Settings | File Templates.
    }

    LuaFieldElementVisitor fieldVisitor = new LuaFieldElementVisitor();

    private static class LuaFieldElementVisitor extends LuaRecursiveElementVisitor {
        Set<String> result = new HashSet<String>();

        @Override
        public void visitIdentifier(LuaIdentifier e) {
            super.visitIdentifier(e);

            if (e instanceof LuaFieldIdentifier && e.getTextLength() > 0 && e.getText().charAt(0) != '[' && e.getName() != null)
                result.add(e.getName());

        }

        public Set<String> getResult() {
            return result;
        }

        public void reset() { result.clear(); }
    }
}