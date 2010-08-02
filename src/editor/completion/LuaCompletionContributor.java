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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import org.jetbrains.annotations.NotNull;



/**
import java.util.*;
public class LuaCompletionContributor extends CompletionContributor {
    private static final Logger log = Logger.getInstance("#Lua.CompletionContributor");
    private Map<String, Set<LuaLookupElement>> contextToDirectiveNameElements = new HashMap<String, Set<LuaLookupElement>>();
        extend(CompletionType.BASIC, psiElement(PsiElement.class), new CompletionProvider<CompletionParameters>() {

            String[] keywords = new String[]{"and", "break", "do", "else",
                    "elseif", "end", "false", "for", "function", "if", "in",
                    "local", "nil", "not", "or", "repeat", "return", "then",
                    "true", "until", "while"};
    {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                	for (int j = 0; j < keywords.length; j++) {
        booleanVariants.add(new LuaLookupElement("false"));
                    }

    private LuaKeywordsManager keywords;

    public LuaCompletionContributor(LuaKeywordsManager keywords) {
        log.info("Created Lua completion contributor");
            
        this.keywords = keywords;

        for (String keyword : keywords.getKeywords()) {
            allLookupElements.add(new LuaLookupElement(keyword));
            }
        });
    }
//  private static final ElementPattern<PsiElement> AFTER_NEW =
//    psiElement().afterLeaf(psiElement().withText(PsiKeyword.NEW).andNot(psiElement().afterLeaf(psiElement().withText(PsiKeyword.THROW))))
//      .withSuperParent(3, GrVariable.class);
    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        if (parameters.getOriginalFile() instanceof LuaPsiFile) {
//  private static final String[] MODIFIERS =
//    new String[]{GrModifier.PRIVATE, GrModifier.PUBLIC, GrModifier.PROTECTED, GrModifier.TRANSIENT, GrModifier.ABSTRACT, GrModifier.NATIVE,
//      GrModifier.VOLATILE, GrModifier.STRICTFP, GrModifier.DEF, GrModifier.FINAL, GrModifier.SYNCHRONIZED, GrModifier.STATIC};
//  private static final ElementPattern<PsiElement> TYPE_IN_VARIABLE_DECLARATION_AFTER_MODIFIER = PlatformPatterns
//    .or(psiElement(PsiElement.class).withParent(GrVariable.class).afterLeaf(MODIFIERS),
            PsiElement parent = parameters.getPosition().getParent();
            log.info("fill completion " + parameters + result);

//                suggestName(result, parent);
//
//    if (!(reference instanceof GrCodeReferenceElement)) return false;
//
//    PsiElement parent = reference.getParent();
//    while (parent instanceof GrCodeReferenceElement) parent = parent.getParent();
//    return parent instanceof GrNewExpression;
//  }
//
//  public LuaCompletionContributor() {
//    extend(CompletionType.BASIC, psiElement(PsiElement.class), new CompletionProvider<CompletionParameters>() {
//      @Override
//      protected void addCompletions(@NotNull CompletionParameters parameters,
//                                    ProcessingContext context,
//                                    @NotNull final CompletionResultSet result) {
//        final PsiElement position = parameters.getPosition();
//        if (reference == null) return;
//        if (isReferenceInNewExpression(reference)) {
//          //reference in new Expression
//          ((GrCodeReferenceElement)reference).processVariants(new Consumer<Object>() {
//            public void consume(Object element) {
//                final PsiClass clazz = (PsiClass)element;
//                final MutableLookupElement<PsiClass> lookupElement = LookupElementFactory.getInstance().createLookupElement(clazz);
//                result.addElement(GroovyCompletionUtil.setTailTypeForConstructor(clazz, lookupElement));
//              }
//              else {
//                result.addElement(LookupItemUtil.objectToLookupItem(element));
//              }
//            }
//          });
//
//        }
//        else if (reference instanceof GrReferenceElement) {
//          ((GrReferenceElement)reference).processVariants(new Consumer<Object>() {
//            public void consume(Object element) {
//              LookupElement lookupElement = LookupItemUtil.objectToLookupItem(element);
//                lookupElement = ((LookupItem)lookupElement).setInsertHandler(new GroovyInsertHandlerAdapter());
//              }
//              result.addElement(lookupElement);
//            }
//          });
//        }
//      }
//    });
//
//    extend(CompletionType.SMART, AFTER_NEW, new CompletionProvider<CompletionParameters>(false) {
//      public void addCompletions(@NotNull final CompletionParameters parameters,
//        final PsiElement identifierCopy = parameters.getPosition();
//        final PsiFile file = parameters.getOriginalFile();
//
//        final List<PsiClassType> expectedClassTypes = new SmartList<PsiClassType>();
//        final List<PsiArrayType> expectedArrayTypes = new ArrayList<PsiArrayType>();
//
//        ApplicationManager.getApplication().runReadAction(new Runnable() {
//          public void run() {
//            PsiType psiType = ((GrVariable)identifierCopy.getParent().getParent().getParent()).getTypeGroovy();
//            if (psiType instanceof PsiClassType) {
//              PsiType type = JavaCompletionUtil.eliminateWildcards(JavaCompletionUtil.originalize(psiType));
//              final PsiClassType classType = (PsiClassType)type;
//                expectedClassTypes.add(classType);
//              expectedArrayTypes.add((PsiArrayType)psiType);
//        });
//
//        for (final PsiArrayType type : expectedArrayTypes) {
//          ApplicationManager.getApplication().runReadAction(new Runnable() {
//              final LookupItem item = (LookupItem)LookupItemUtil.objectToLookupItem(JavaCompletionUtil.eliminateWildcards(type));
//              item.setAttribute(LookupItem.DONT_CHECK_FOR_INNERS, "");
//                JavaCompletionUtil.setShowFQN(item);
//              item.setInsertHandler(new ArrayInsertHandler());
//              result.addElement(item);
//            }
//          });
//        }
//        JavaSmartCompletionContributor.processInheritors(parameters, identifierCopy, file, expectedClassTypes, new Consumer<PsiType>() {
//          public void consume(final PsiType type) {
//            addExpectedType(result, type, identifierCopy);
//          }
//        }, result.getPrefixMatcher());
//      }
//    });
//    //provide 'this' and 'super' completions in ClassName.<caret>
//    extend(CompletionType.BASIC, AFTER_DOT, new CompletionProvider<CompletionParameters>() {
//      @Override
//      protected void addCompletions(@NotNull CompletionParameters parameters,
//                                    ProcessingContext context,
//                                    @NotNull CompletionResultSet result) {
        }
//        assert position.getParent() instanceof GrReferenceExpression;
//        final GrReferenceExpression refExpr = ((GrReferenceExpression)position.getParent());
//        final GrExpression qualifier = refExpr.getQualifierExpression();
//        if (!(qualifier instanceof GrReferenceExpression)) return;
//        GrReferenceExpression referenceExpression = (GrReferenceExpression)qualifier;
//        final PsiElement resolved = referenceExpression.resolve();
//        if (!(resolved instanceof PsiClass)) return;
//        if (!org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil.hasEnclosingInstanceInScope((PsiClass)resolved, position, false)) return;
    }
//        for (String keyword : THIS_SUPER) {
//          final LookupItem item = (LookupItem)LookupItemUtil.objectToLookupItem(keyword);
//          item.setAttribute(LookupItem.DONT_CHECK_FOR_INNERS, "");
//          result.addElement(item);
//        }
//      }
//    });
//    extend(CompletionType.BASIC, TYPE_IN_VARIABLE_DECLARATION_AFTER_MODIFIER, new CompletionProvider<CompletionParameters>() {
//      @Override
    private void suggestName(CompletionResultSet result, LuaNamedElement where) {
//                                    ProcessingContext context,
//                                    @NotNull CompletionResultSet result) {
//        final PsiElement position = parameters.getPosition();
//        if (!GroovyCompletionUtil.isFirstElementAfterModifiersInVariableDeclaration(position, true)) return;
//        for (Object variant : new ClassesGetter().get(parameters.getPosition(), null)) {
//          final String lookupString;
//          if (variant instanceof PsiElement) {
//            lookupString = PsiUtilBase.getName(((PsiElement)variant));
//          }
//          else {
//            lookupString = variant.toString();
//          }
//          if (lookupString == null) continue;
    }
//          LookupElementBuilder builder = LookupElementBuilder.create(variant, lookupString);
//          if (variant instanceof Iconable) {
//            builder = builder.setIcon(((Iconable)variant).getIcon(Iconable.ICON_FLAG_VISIBILITY));
//          }
    private void suggestValue(CompletionResultSet result, LuaNamedElement where) {
//            String packageName = PsiFormatUtil.getPackageDisplayName((PsiClass)variant);
//            builder = builder.setTailText(" (" + packageName + ")", true);
//          }
//          builder.setInsertHandler(new GroovyInsertHandler());
//          result.addElement(builder);
//        }
//      }
//    });
//  }
//  private static boolean checkForInnerClass(PsiClass psiClass, PsiElement identifierCopy) {
//    return !PsiUtil.isInnerClass(psiClass) ||
//           org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil
//             .hasEnclosingInstanceInScope(psiClass.getContainingClass(), identifierCopy, true);
    }
//  private static void addExpectedType(final CompletionResultSet result, final PsiType type, final PsiElement place) {
    private void suggestVariable(CompletionResultSet result) {
//    final PsiClass psiClass = PsiUtil.resolveClassInType(type);
//    if (psiClass == null) return;
    }
//    if (psiClass.isInterface() || psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) return;
//    if (!checkForInnerClass(psiClass, place)) return;
//    final LookupItem item = (LookupItem)LookupItemUtil.objectToLookupItem(JavaCompletionUtil.eliminateWildcards(type));
//    item.setAttribute(LookupItem.DONT_CHECK_FOR_INNERS, "");
//    JavaCompletionUtil.setShowFQN(item);
//    item.setInsertHandler(new AfterNewClassInsertHandler((PsiClassType)type, place));
//    result.addElement(item);
//  }
//  public void beforeCompletion(@NotNull final CompletionInitializationContext context) {
//    final PsiFile file = context.getFile();
//    final Project project = context.getProject();
//    JavaCompletionUtil.initOffsets(file, project, context.getOffsetMap());
//    if (context.getCompletionType() == CompletionType.BASIC && file instanceof GroovyFile) {
//      if (semicolonNeeded(context)) {
//        context.setFileCopyPatcher(new DummyIdentifierPatcher(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED + ";"));
//      }
//      else if (isInClosurePropertyParameters(context)) {
//        context.setFileCopyPatcher(new DummyIdentifierPatcher(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED + "->"));
//      }
//    }
//  }
//
//  private static boolean isInClosurePropertyParameters(CompletionInitializationContext context) { //Closure cl={String x, <caret>...
//    final PsiFile file = context.getFile();                                                       //Closure cl={String x, String <caret>...
//    final PsiElement position = file.findElementAt(context.getStartOffset());
//    if (position == null) return false;
//
//    GrVariableDeclaration declaration = PsiTreeUtil.getParentOfType(position, GrVariableDeclaration.class, false, GrStatement.class);
//    if (declaration == null) {
//      PsiElement prev = position.getPrevSibling();
//      prev = skipWhitespaces(prev, false);
//      if (prev instanceof PsiErrorElement) {
//        prev = prev.getPrevSibling();
//      }
//      prev = skipWhitespaces(prev, false);
//      if (prev instanceof GrVariableDeclaration) declaration = (GrVariableDeclaration)prev;
//    }
//    if (declaration != null) {
//      if (!(declaration.getParent() instanceof GrClosableBlock)) return false;
//      PsiElement prevSibling = skipWhitespaces(declaration.getPrevSibling(), false);
//      return prevSibling instanceof GrParameterList;
//    }
//    return false;
//  }
//
//  private static boolean semicolonNeeded(CompletionInitializationContext context) { //<caret>String name=
//    HighlighterIterator iterator = ((EditorEx)context.getEditor()).getHighlighter().createIterator(context.getStartOffset());
//    if (iterator.atEnd()) return false;
//
//    if (iterator.getTokenType() == GroovyTokenTypes.mIDENT) {
//      iterator.advance();
//    }
//
//    if (!iterator.atEnd() && iterator.getTokenType() == GroovyTokenTypes.mLPAREN) {
//      return true;
//    }
//
//    while (!iterator.atEnd() && GroovyTokenTypes.WHITE_SPACES_OR_COMMENTS.contains(iterator.getTokenType())) {
//      iterator.advance();
//    }
//
//    if (iterator.atEnd() || iterator.getTokenType() != GroovyTokenTypes.mIDENT) return false;
//    iterator.advance();
//
//    while (!iterator.atEnd() && GroovyTokenTypes.WHITE_SPACES_OR_COMMENTS.contains(iterator.getTokenType())) {
//      iterator.advance();
//    }
////    if (iterator.atEnd()) return true;
//
////    return iterator.getTokenType() == GroovyTokenTypes.mASSIGN;
//    return true;
//  }
}