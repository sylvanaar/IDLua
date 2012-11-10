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
import com.intellij.openapi.diagnostic.*;
import com.intellij.openapi.util.*;
import com.intellij.patterns.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.psi.util.*;
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.lang.lexer.*;
import com.sylvanaar.idea.Lua.lang.parser.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.lists.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import com.sylvanaar.idea.Lua.options.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static com.intellij.patterns.PlatformPatterns.*;

public class LuaCompletionContributor extends DefaultCompletionContributor {
    private static final Logger log = Logger.getInstance("Lua.CompletionContributor");

    private static final ElementPattern<PsiElement>            AFTER_SELF_DOT =
            psiElement().withParent(LuaCompoundIdentifier.class).afterSibling(psiElement().withName("self"));
    private static final PsiElementPattern.Capture<PsiElement> AFTER_DOT      = psiElement().afterLeaf(".", ":");
    private static final PsiElementPattern.Capture<PsiElement> AFTER_COLON    = psiElement().afterLeaf(":");

    private static final ElementPattern<PsiElement> AFTER_FUNCTION =
            psiElement().afterLeafSkipping(psiElement().whitespace(), PlatformPatterns.string().matches("function"));

    private static final ElementPattern<PsiElement> NOT_AFTER_DOT =
            psiElement().withParent(LuaIdentifier.class).andNot(psiElement().afterLeaf(".", ":"))
                    .andNot(psiElement().withSuperParent(2, LuaFieldIdentifier.class))
                    .andNot(psiElement().withSuperParent(2, LuaIdentifierList.class))
                    .andNot(psiElement().withSuperParent(2, LuaFunctionDefinition.class))
                    .andNot(psiElement().withSuperParent(2, LuaReferenceElement.class)
                            .withParent(LuaIdentifierList.class));

    private static final ElementPattern<PsiElement> AFTER_LOCAL = psiElement()
            .afterLeafSkipping(psiElement().whitespace(),
                    psiElement().withElementType(LuaTokenTypes.LOCAL));


    private static final Key<Collection<LuaDeclarationExpression>> PREFIX_FILTERED_GLOBALS_COLLECTION =
            new Key<Collection<LuaDeclarationExpression>>("lua.prefix.globals");
    private static final Key<Collection<LuaDeclarationExpression>> PREFIX_FILTERED_COMPOUND_COLLECTION =
            new Key<Collection<LuaDeclarationExpression>>("lua.prefix.compounds");

    private static final Key<Set<String>> USED_NEARBY_GLOBALS = new Key<Set<String>>("lua.used.nearby.locals");

    private static final ElementPattern<PsiElement> REFERENCES =
            psiElement().andOr(
                    psiElement().withParent(LuaLocalIdentifier.class),
                    psiElement().withParent(LuaGlobalIdentifier.class));

    private static final ElementPattern<PsiElement> NAME =
            psiElement().withParent(LuaIdentifier.class).andNot(psiElement().withParent(LuaCompoundIdentifier.class));

    public static final PsiElementPattern.Capture<PsiElement> AFTER_QUALIFIER =
            AFTER_DOT.withParent(LuaCompoundIdentifier.class);

    private static final ElementPattern<PsiElement> FIELDS =
            psiElement().andOr(psiElement().withParent(LuaFieldIdentifier.class), AFTER_QUALIFIER);

    private static final ElementPattern<PsiElement> INDEXED_FIELDS =
            psiElement().afterSibling(REFERENCES).and(psiElement().afterLeaf("["));

    private static final ElementPattern<PsiElement> STRING_LITERAL_CALL =
            psiElement().afterSibling(psiElement().withElementType(LuaElementTypes.FUNCTION_CALL_EXPR))
                    .afterLeafSkipping(psiElement().whitespace(), AFTER_DOT);


    private Collection<LuaDeclarationExpression> getAllGlobals(@NotNull CompletionParameters parameters,
                                                               ProcessingContext context) {
        return LuaPsiManager.getInstance(parameters.getOriginalFile().getProject()).getFilteredGlobalsCache();
    }

    private Collection<LuaDeclarationExpression> getPrefixFilteredGlobals(String prefix,
                                                                          @NotNull CompletionParameters parameters,
                                                                          ProcessingContext context) {
        Collection<LuaDeclarationExpression> names = context.get(PREFIX_FILTERED_GLOBALS_COLLECTION);
        if (names != null) return names;

        names = new ArrayList<LuaDeclarationExpression>();

        List<String> used = new ArrayList<String>();
        used.add("...");

        int prefixLen = prefix.length();
        for (LuaDeclarationExpression key1 : getAllGlobals(parameters, context)) {
            if (key1 instanceof LuaCompoundIdentifier) continue;
            String key = key1.getDefinedName();
            if (key != null && key.length() > prefixLen && key.startsWith(prefix) && !used.contains(key)) {
                names.add(key1);
                used.add(key);
            }
        }

        context.put(PREFIX_FILTERED_GLOBALS_COLLECTION, names);
        return names;
    }

    private Collection<LuaDeclarationExpression> getPrefixFilteredCompoundIds(String prefix,
                                                                              @NotNull CompletionParameters parameters,
                                                                              ProcessingContext context) {
        Collection<LuaDeclarationExpression> names = context.get(PREFIX_FILTERED_COMPOUND_COLLECTION);
        if (names != null) return names;

        names = new ArrayList<LuaDeclarationExpression>();

        List<String> used = new ArrayList<String>();

        int prefixLen = prefix.length();
        for (LuaDeclarationExpression key1 : getAllGlobals(parameters, context)) {
            if (key1 instanceof LuaCompoundIdentifier) {

                String key = key1.getDefinedName();
                if (key != null && key.length() > prefixLen && key.startsWith(prefix) && !used.contains(key)) {
                    names.add(key1);
                    used.add(key);
                }
            }
        }
        context.put(PREFIX_FILTERED_COMPOUND_COLLECTION, names);
        return names;
    }

    public LuaCompletionContributor() {
        log.debug(NOT_AFTER_DOT.toString());

        extend(CompletionType.BASIC, AFTER_LOCAL, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                result.addElement(LuaLookupElement.createKeywordElement(LuaKeywordsManager.FUNCTION));
            }
        });
        extend(CompletionType.BASIC, NOT_AFTER_DOT, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                for (String s : LuaKeywordsManager.getKeywords())
                    result.addElement(LuaLookupElement.createKeywordElement(s));
            }
        });


        extend(CompletionType.BASIC, REFERENCES, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                String prefix = result.getPrefixMatcher().getPrefix();
                if (prefix.length() == 0) return;

                for (LuaDeclarationExpression key : getPrefixFilteredGlobals(prefix, parameters, context)) {
                    if (key.isValid()) result.addElement(LuaLookupElement.createElement(key));
                }

                addUsedNeabyGlobals(parameters, context, result);
            }
        });

        extend(CompletionType.BASIC, FIELDS, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement originalPosition = parameters.getOriginalPosition();
                LuaCompoundIdentifier fieldOf = PsiTreeUtil
                        .getParentOfType(ObjectUtils.chooseNotNull(originalPosition, parameters.getPosition()),
                                LuaCompoundIdentifier.class);
                if (fieldOf == null) return;
                LuaExpression left = fieldOf.getLeftSymbol();
                String prefix = result.getPrefixMatcher().getPrefix();

                if (left.getLuaType() instanceof LuaTable && !left.textMatches("_G")) {
                    final Map<?, ?> fieldSet = ((LuaTable) left.getLuaType()).getFieldSet();
                    if (fieldSet.size() > 0) {
                        for (Object f : fieldSet.keySet())
                            if (f instanceof String && ((String) f).startsWith(prefix))
                                result.addElement(LuaLookupElement.createTypedElement((String) f));

                        result.stopHere();
                        return;
                    }
                } else if (left.getLuaType() == LuaPrimitiveType.STRING) {
                    for (LuaDeclarationExpression key : getPrefixFilteredGlobals("string.", parameters, context)) {
                        final String name = key.getName();
                        if (key.isValid() && name != null && name.startsWith(prefix))
                            if (left instanceof LuaStringLiteralExpressionImpl) result.addElement(LuaLookupElement
                                    .createStringMetacallElement(name, (LuaStringLiteralExpressionImpl) left, key));
                            else result.addElement(LuaLookupElement.createTypedElement(name));
                    }
                    result.stopHere();
                    return;
                }

                final PsiElement prevSibling = originalPosition != null ? originalPosition.getPrevSibling() : null;

                LuaCompoundIdentifier outer = fieldOf.getEnclosingIdentifier();
                while (outer != fieldOf) {
                    fieldOf = outer;
                    outer = fieldOf.getEnclosingIdentifier();
                }

                prefix = outer.getText() + (prevSibling != null ? prevSibling.getText() : "") + prefix;
                final int length = outer.getText().length();
                for (LuaDeclarationExpression key : getPrefixFilteredGlobals(prefix, parameters, context)) {
                    if (key.isValid())
                        result.addElement(LuaLookupElement.createElement(key.getText().substring(length + 1)));
                }

//                prefix = outer.getText() + (prevSibling != null ? prevSibling.getText() :"")+ prefix;
//                for (LuaDeclarationExpression key : getPrefixFilteredCompoundIds(prefix, parameters, context)) {
//                    assert key instanceof LuaCompoundIdentifier;
//                    if (key.isValid()) result.addElement(LuaLookupElement.createElement(((LuaCompoundIdentifier)
// key).getRightSymbol()));
//                }

//                final PsiElement context1 = parameters.getPosition().getContext();
//                if (context1 != null) {
//                    final LuaExpression leftSymbol =
//                            ((LuaFieldIdentifier) context1)
//                                    .getEnclosingIdentifier().getLeftSymbol();
//
//                    final int length = leftSymbol.getText().length();
//                    for (LuaDeclarationExpression key : getPrefixFilteredCompoundIds(leftSymbol.getText(),
// parameters, context)) {
////                        assert key instanceof LuaCompoundIdentifier;
//                        if (key.isValid()) {
//                            result.addElement(LuaLookupElement.createElement(key.getText().substring(length+1)));
//                        }
//                    }
//
//                }

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

        extend(CompletionType.BASIC, NOT_AFTER_DOT, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                addUsedNeabyGlobals(parameters, context, result);
            }
        });

//        // Completions available through ":" after a string literal
        extend(CompletionType.BASIC, AFTER_COLON, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                PsiElement element = parameters.getPosition();
                if (element instanceof LeafPsiElement) element = element.getParent();
                if (element instanceof LuaFieldIdentifier) element = element.getParent();
                if (element instanceof LuaCompoundIdentifier)
                    element = ((LuaCompoundIdentifier) element).getLeftSymbol();
                if (!(element instanceof LuaFunctionCallExpression)) return;

                final LuaExpressionList argumentList = ((LuaFunctionCallExpression) element).getArgumentList();
                if (argumentList == null) return;

                final List<LuaExpression> luaExpressions = argumentList.getLuaExpressions();

                if (luaExpressions.size() == 1 && luaExpressions.get(0) instanceof LuaStringLiteralExpressionImpl) {
                    String prefix = result.getPrefixMatcher().getPrefix();
                    for (LuaDeclarationExpression key : getPrefixFilteredGlobals("string.", parameters, context)) {
                        if (key.isValid()) result.addElement(LuaLookupElement
                                .createStringMetacallElement(key.getDefinedName(),
                                        (LuaStringLiteralExpressionImpl) luaExpressions.get(0), key));
                    }

                    result.stopHere();
                }
            }
        });
    }

    private void addUsedNeabyGlobals(CompletionParameters parameters, ProcessingContext context,
                                     CompletionResultSet result) {
        if (!LuaApplicationSettings.getInstance().INCLUDE_ALL_FIELDS_IN_COMPLETIONS) return;
        if (context.get(USED_NEARBY_GLOBALS) != null) return;

        LuaPsiFile file = (LuaPsiFile) parameters.getOriginalFile();

        globalUsageVisitor.reset();
        context.put(USED_NEARBY_GLOBALS, globalUsageVisitor.getResult());

        file.acceptChildren(globalUsageVisitor);
        String prefix = result.getPrefixMatcher().getPrefix();
        int prefixLen = prefix.length();

        for (String key : globalUsageVisitor.getResult()) {

            if (key.length() > prefixLen && key.startsWith(prefix))
                result.addElement(LuaLookupElement.createNearbyUsageElement(key));
        }
    }

    public static final OffsetKey IDENTIFIER_START_OFFSET = OffsetKey.create("identifierEnd");

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {

        context.setDummyIdentifier(CompletionInitializationContext.DUMMY_IDENTIFIER.trim() + ";");

//        final PsiFile file = context.getFile();
//
//        final char c1 = file.getText().charAt(context.getStartOffset() - 1);
//        if (c1 == ':' || c1 == '.') {
//
//            final PsiReference e = file.findReferenceAt(context.getStartOffset() - 2);
//
//            if (e != null) {
//                int newStart = context.getStartOffset();
//
//                if (e.getElement() instanceof LuaCompoundIdentifier) {
//                    LuaCompoundIdentifier c = (LuaCompoundIdentifier) e.getElement();
//
//                    LuaExpression s = c.getLeftSymbol();
//                    if (s != null)
//                        newStart = s.getTextOffset();
//                } else {
//                    if (e.getElement() instanceof LuaIdentifier)
//                        newStart = e.getElement().getTextOffset();
//                }
//                context.getOffsetMap().addOffset(IDENTIFIER_START_OFFSET, newStart);
//            }
//        }
    }

    @Override
    public void duringCompletion(@NotNull CompletionInitializationContext context) {
        super.duringCompletion(context);
    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        final PsiFile file = parameters.getOriginalFile();
        if (!(file instanceof LuaPsiFile)) return;


//        final int offset = parameters.getOffset();
//        final char c1 = file.getText().charAt(offset-1);
//        if (c1 == ':' || c1 == '.') {
//
//            final PsiReference e = file.findReferenceAt(offset - 2);
//
//            if (e != null) {
//                final PsiElement element = e.getElement();
//                if (element instanceof LuaCompoundIdentifier) {
//                    LuaCompoundIdentifier c = (LuaCompoundIdentifier) element;
//
//                    LuaExpression s = c.getLeftSymbol();
//                    if (s != null) parameters = parameters.withPosition(s, offset);
//                } else {
//                    if (element instanceof LuaIdentifier) parameters = parameters.withPosition(element, offset);
//                }
//            }
//        }

        result.restartCompletionWhenNothingMatches();
        super.fillCompletionVariants(parameters,
                result);    //To change body of overridden methods use File | Settings | File Templates.
    }

    LuaFieldElementVisitor fieldVisitor       = new LuaFieldElementVisitor();
    LuaGlobalUsageVisitor  globalUsageVisitor = new LuaGlobalUsageVisitor();

    private class LuaFieldElementVisitor extends LuaRecursiveElementVisitor {
        Set<String> result = new HashSet<String>();

        @Override
        public void visitIdentifier(LuaIdentifier e) {
            super.visitIdentifier(e);

            if (e instanceof LuaFieldIdentifier && e.getTextLength() > 0 && e.getText().charAt(0) != '[' &&
                e.getName() != null) result.add(e.getName());

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

            if (e instanceof LuaGlobalIdentifier && e.getTextLength() > 0 && e.getName() != null &&
                !e.getName().equals("...")) result.add(e.getName());
        }

        public Set<String> getResult() {
            return result;
        }

        public void reset() {
            result.clear();
        }
    }
}