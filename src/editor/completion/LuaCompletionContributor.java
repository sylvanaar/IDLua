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
import com.intellij.openapi.util.Key;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.ProcessingContext;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiManager;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaStringLiteralExpressionImpl;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class LuaCompletionContributor extends DefaultCompletionContributor {
    private static final Logger log = Logger.getInstance("Lua.CompletionContributor");

    private static final ElementPattern<PsiElement> AFTER_SELF_DOT = psiElement().withParent(LuaCompoundIdentifier.class).afterSibling(psiElement().withName("self"));
    private static final PsiElementPattern.Capture<PsiElement> AFTER_DOT = psiElement().afterLeaf(".", ":");
    private static final PsiElementPattern.Capture<PsiElement> AFTER_COLON = psiElement().afterLeaf(":");

    private static final ElementPattern<PsiElement> AFTER_FUNCTION = psiElement().afterLeafSkipping(psiElement().whitespace(), PlatformPatterns.string().matches("function"));

    private static final ElementPattern<PsiElement> NOT_AFTER_DOT = psiElement().withParent(LuaIdentifier.class).andNot(psiElement().afterLeaf(".", ":"));

    private static final Key<Collection<LuaDeclarationExpression>> PREFIX_FILTERED_GLOBALS_COLLECTION = new Key<Collection<LuaDeclarationExpression>>("lua.prefix.globals");

    private static final ElementPattern<PsiElement> REFERENCES =
            psiElement().withParent(LuaIdentifier.class);

    private static final ElementPattern<PsiElement> NAME =
            psiElement().withParent(LuaIdentifier.class).andNot(psiElement().withParent(LuaCompoundIdentifier.class));

    public static final PsiElementPattern.Capture<PsiElement> AFTER_QUALIFIER = AFTER_DOT.withParent(LuaCompoundIdentifier.class);

    private static final ElementPattern<PsiElement> FIELDS =
            psiElement().andOr(psiElement().withParent(LuaFieldIdentifier.class),
                    AFTER_QUALIFIER);

    private static final ElementPattern<PsiElement> INDEXED_FIELDS =
            psiElement().afterSibling(REFERENCES).and(psiElement().afterLeaf("["));

    private static final ElementPattern<PsiElement> STRING_LITERAL_CALL =
            psiElement().afterSibling(psiElement().withElementType(LuaElementTypes.FUNCTION_CALL_EXPR)).afterLeafSkipping(psiElement().whitespace(), AFTER_DOT);


    private Collection<LuaDeclarationExpression> getAllGlobals(@NotNull CompletionParameters parameters, ProcessingContext context) {
        return LuaPsiManager.getInstance(parameters.getOriginalFile().getProject()).getFilteredGlobalsCache();
    }

    private Collection<LuaDeclarationExpression> getPrefixFilteredGlobals(String prefix, @NotNull CompletionParameters parameters, ProcessingContext context) {
        Collection<LuaDeclarationExpression> names = context.get(PREFIX_FILTERED_GLOBALS_COLLECTION);
        if (names != null) return names;

        names = new ArrayList<LuaDeclarationExpression>();

        int prefixLen = prefix.length();
        for (LuaDeclarationExpression key1 : getAllGlobals(parameters, context)) {
            String key = key1.getDefinedName();
            if (key != null && key.length() > prefixLen && key.startsWith(prefix))
                names.add(key1);
        }

        context.put(PREFIX_FILTERED_GLOBALS_COLLECTION, names);
        return names;
    }

    public LuaCompletionContributor() {
        extend(CompletionType.BASIC, NOT_AFTER_DOT, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                for (String s : LuaKeywordsManager.getKeywords())
                    result.addElement(LuaLookupElement.createElement(s));
            }
        });


        extend(CompletionType.BASIC, REFERENCES, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                String prefix = result.getPrefixMatcher().getPrefix();

                for (LuaDeclarationExpression key : getPrefixFilteredGlobals(prefix, parameters, context)) {
                    if (key.isValid())
                        result.addElement(LuaLookupElement.createElement(key));
                }
            }
        });

        extend(CompletionType.BASIC, FIELDS, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                String prefix = result.getPrefixMatcher().getPrefix();

                for (LuaDeclarationExpression key : getPrefixFilteredGlobals(prefix, parameters, context)) {
                    if (key.isValid()) result.addElement(LuaLookupElement.createElement(key));
                }
            }
        });

        extend(CompletionType.BASIC, INDEXED_FIELDS, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                String prefix = result.getPrefixMatcher().getPrefix();

                PsiElement pos = parameters.getPosition();

            }
        });

        extend(CompletionType.BASIC, AFTER_COLON, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement element = parameters.getPosition();
                if (element instanceof LeafPsiElement)
                    element = element.getParent();
                if (element instanceof LuaFieldIdentifier)
                    element = element.getParent();
                if (element instanceof LuaCompoundIdentifier)
                    element = ((LuaCompoundIdentifier) element).getLeftSymbol();
                if (!(element instanceof LuaFunctionCallExpression))
                    return;

                final LuaExpressionList argumentList = ((LuaFunctionCallExpression) element).getArgumentList();
                if (argumentList == null) return;

                final List<LuaExpression> luaExpressions = argumentList.getLuaExpressions();

                if (luaExpressions.size() == 1 && luaExpressions.get(0) instanceof LuaStringLiteralExpressionImpl) {
                    String prefix = result.getPrefixMatcher().getPrefix();
                    for (LuaDeclarationExpression key : getPrefixFilteredGlobals("string.", parameters, context)) {
                        if (key.isValid())
                            result.addElement(LuaLookupElement.createStringMetacallElement(prefix, (LuaStringLiteralExpressionImpl) luaExpressions.get(0), key));
                    }
                }
            }
        });
    }


//    @Override
//    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
//        int end = context.getIdentifierEndOffset();
//        int start = context.getStartOffset();
//        String identifierToReplace = context.getEditor().getDocument().getText(new TextRange(start-1, end));
//
//        if (identifierToReplace.charAt(0) == '.' || identifierToReplace.charAt(0) == ':')
//            context.setReplacementOffset(start);
//
//        super.beforeCompletion(context);
//    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);    //To change body of overridden methods use File | Settings | File Templates.
    }

    LuaFieldElementVisitor fieldVisitor = new LuaFieldElementVisitor();
    LuaGlobalUsageVisitor globalUsageVisitor = new LuaGlobalUsageVisitor();

    private class LuaFieldElementVisitor extends LuaRecursiveElementVisitor {
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

        public void reset() {
            result.clear();
        }
    }


    private class LuaGlobalUsageVisitor extends LuaRecursiveElementVisitor {
        Set<String> result = new HashSet<String>();

        @Override
        public void visitIdentifier(LuaIdentifier e) {
            super.visitIdentifier(e);

            if (e instanceof LuaGlobalIdentifier && e.getTextLength() > 0 && e.getName() != null)
                result.add(e.getName());
        }

        public Set<String> getResult() {
            return result;
        }

        public void reset() {
            result.clear();
        }
    }
}