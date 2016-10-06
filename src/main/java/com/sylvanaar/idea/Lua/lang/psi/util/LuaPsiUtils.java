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
package com.sylvanaar.idea.Lua.lang.psi.util;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaAnonymousFunctionExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaStubElementBase;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaFunction;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;


/**
 * User: jansorg
 * Date: 04.08.2009
 * Time: 21:45:47
 */
public final class LuaPsiUtils {
    public static ItemPresentation getFunctionPresentation(
        final LuaPsiElement e) {
        return new ItemPresentation() {
                public String getPresentableText() {
                    return e.getPresentationText();
                }

                @Nullable
                public String getLocationString() {
                    String name = e.getContainingFile().getName();

                    return "(in " + name + ")";
                }

                @Nullable
                public Icon getIcon(boolean open) {
                    return LuaIcons.LUA_FUNCTION;
                }

                @Nullable
                public TextAttributesKey getTextAttributesKey() {
                    return null;
                }
            };
    }

    /**
     * Returns the depth in the tree this element has.
     *
     * @param element The element to lookup
     * @return The depth, 0 if it's at the top level
     */
    public static int nestingLevel(PsiElement element) {
        int depth = 0;

        PsiElement current = element.getContext();

        while (current != null) {
            depth++;
            current = current.getContext();
        }

        return depth;
    }

    /**
     * Returns the depth in blocks this element has in the tree.
     *
     * @param element The element to lookup
     * @return The depth measured in blocks, 0 if it's at the top level
     */
    public static int blockNestingLevel(PsiElement element) {
        int depth = 0;

        PsiElement current = findEnclosingBlock(element);

        while (current != null) {
            depth++;
            current = findEnclosingBlock(current);
        }

        return depth;
    }

    /**
     * Returns the element after the given element. It may either be the next sibling or the
     * next logical element after the given element (i.e. the element after the parent context).
     * If it's the last element of the file null is retunred.
     *
     * @param element The element to check
     * @return Next element or null
     */
    @Nullable
    public static PsiElement elementAfter(PsiElement element) {
        ASTNode node = element.getNode();
        ASTNode next = (node != null) ? node.getTreeNext() : null;

        return (next != null) ? next.getPsi() : null;

        /*if (element == null) return null;
        
        PsiElement next = element.getNextSibling();
        if (next != null) {
            return next;
        }
        
        //check parent
        return elementAfter(element.getContext());*/
    }

    /**
     * Returns the next logical block which contains this element.
     *
     * @param element The element to check
     * @return The containing block or null
     */
    public static PsiElement findEnclosingBlock(PsiElement element) {
        while ((element != null) && (element.getContext() != null)) {
            element = element.getContext();

            if (isValidContainer(element)) {
                return element;
            }
        }

        return null;
    }

    private static boolean isValidContainer(PsiElement element) {
        return element instanceof LuaBlock;
    }

    public static boolean processChildDeclarationsS(
        PsiElement parentContainer, PsiScopeProcessor processor,
        ResolveState resolveState, PsiElement parent, PsiElement place) {
        PsiElement child = parentContainer.getFirstChild();

        while (child != null) {
            if (!child.processDeclarations(processor, resolveState, parent,
                        place)) {
                return false;
            }

            child = child.getNextSibling();
        }

        return true;
    }

    public static boolean processChildDeclarations(PsiElement element,
        PsiScopeProcessor processor, ResolveState substitutor,
        PsiElement lastParent, PsiElement place) {
        PsiElement run = (lastParent == null) ? element.getLastChild()
                                              : lastParent.getPrevSibling();

        while (run != null) {
            if (!run.processDeclarations(processor, substitutor, null, place)) {
                return false;
            }

            run = run.getPrevSibling();
        }

        return true;
    }

    public static int getElementLineNumber(PsiElement element) {
        FileViewProvider fileViewProvider = element.getContainingFile()
                                                   .getViewProvider();

        if (fileViewProvider.getDocument() != null) {
            return fileViewProvider.getDocument()
                                   .getLineNumber(element.getTextOffset()) + 1;
        }

        return 0;
    }

    public static int getElementEndLineNumber(PsiElement element) {
        FileViewProvider fileViewProvider = element.getContainingFile()
                                                   .getViewProvider();

        if (fileViewProvider.getDocument() != null) {
            return fileViewProvider.getDocument()
                                   .getLineNumber(element.getTextOffset() +
                element.getTextLength()) + 1;
        }

        return 0;
    }

    @Nullable
    public static LuaPsiElement getCoveringPsiElement(
        @NotNull
    final PsiElement psiElement) {
        PsiElement current = psiElement;

        while (current != null) {
            if (current instanceof LuaPsiElement) {
                return (LuaPsiElement) current;
            }

            current = current.getParent();
        }

        return null;
    }

    public static TextRange createRange(PsiElement node) {
        return TextRange.from(node.getTextOffset(), node.getTextLength());
    }

    public static IElementType nodeType(PsiElement element) {
        ASTNode node = element.getNode();

        if (node == null) {
            return null;
        }

        return node.getElementType();
    }

    public static PsiElement findNextSibling(PsiElement start,
        IElementType ignoreType) {
        PsiElement current = start.getNextSibling();

        while (current != null) {
            if (ignoreType != nodeType(current)) {
                return current;
            }

            current = current.getNextSibling();
        }

        return null;
    }

    public static PsiElement findPreviousSibling(PsiElement start,
        IElementType ignoreType) {
        PsiElement current = start.getPrevSibling();

        while (current != null) {
            if (ignoreType != nodeType(current)) {
                return current;
            }

            current = current.getPrevSibling();
        }

        return null;
    }

    /**
     * Replaces the priginal element with the replacement.
     *
     * @param original    The original element which should be replaced.
     * @param replacement The new element
     * @return The replaces element. Depending on the context of the original element it either the original element
     * or the replacement element.
     * @throws com.intellij.util.IncorrectOperationException
     *          cant do it
     */
    public static PsiElement replaceElement(PsiElement original,
        PsiElement replacement) throws IncorrectOperationException {
        try {
            try {
                return original.replace(replacement);
            } catch (IncorrectOperationException e) {
                //failed, try another way
            } catch (UnsupportedOperationException e) {
                //failed, try another way
            }

            PsiElement parent = original.getParent();

            if (parent != null) {
                PsiElement inserted = parent.addBefore(replacement, original);
                original.delete();

                return inserted;
            } else {
                //last try, not optimal
                original.getNode()
                        .replaceAllChildrenToChildrenOf(replacement.getNode());

                return original;
            }
        } finally {
        }
    }

    public static boolean isLValue(LuaPsiElement element) {
        if (element instanceof LuaStubElementBase) {
            assert ((LuaStubElementBase) element).getStub() == null : "Operating on a stub";
        }

        if (element instanceof LuaReferenceElement) {
            if (!(element instanceof LuaFieldIdentifier) && element.getParent() instanceof LuaIdentifierList) {
                return checkForErrors((LuaReferenceElement) element);
            }

            final PsiElement element1 = ((LuaReferenceElement) element).getElement();

            if (element1 instanceof LuaCompoundIdentifier) {
                return ((LuaCompoundIdentifier) element1).isCompoundDeclaration();
            }

//        if (element1 instanceof LuaDeclarationExpression)
//            return true;

          if (element1 instanceof LuaSymbol && ((LuaSymbol) element1).isAssignedTo())
              return true;
        }

        if (element instanceof LuaSymbol) {
            final LuaReferenceElement reference = (LuaReferenceElement) element.getReference();

            if ((reference != null) &&
                    reference.getParent() instanceof LuaIdentifierList) {
                return checkForErrors(reference);
            }
        }

        return false;
    }

    private static boolean checkForErrors(LuaReferenceElement reference) {
        final LuaIdentifierList identifierList = (LuaIdentifierList) reference.getParent();

        if (identifierList.getParent() instanceof LuaAssignmentStatement) {
            return !PsiTreeUtil.hasErrorElements(identifierList.getParent());
        }

        return true;
    }

    //    @NotNull
    //  public static LuaDocPsiElement[] toPsiElementArray(@NotNull Collection<? extends LuaDocPsiElement> collection) {
    //    if (collection.isEmpty()) return LuaDocPsiElement.EMPTY_ARRAY;
    //    return collection.toArray(new LuaDocPsiElement[collection.size()]);
    //  }
    //  @NotNull
    //  public static LuaPsiElement[] toPsiElementArray(@NotNull Collection<? extends LuaPsiElement> collection) {
    //    if (collection.isEmpty()) return LuaPsiElement.EMPTY_ARRAY;
    //    return collection.toArray(new LuaPsiElement[collection.size()]);
    //  }
    @NotNull
    public static PsiElement[] toPsiElementArray(
        @NotNull
    Collection<?extends PsiElement> collection) {
        if (collection.isEmpty()) {
            return PsiElement.EMPTY_ARRAY;
        }

        return collection.toArray(new PsiElement[collection.size()]);
    }

    public static boolean hasDirectChildErrorElements(
        @NotNull
    final PsiElement element) {
        if (element instanceof PsiErrorElement) {
            return true;
        }

        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiErrorElement) {
                return true;
            }
        }

        return false;
    }

    public static class LuaBlockReturnVisitor extends LuaRecursiveElementVisitor {
        public LuaFunction myType;

        public LuaBlockReturnVisitor(LuaFunction type) {
            myType = type;
        }

        @Override
        public void visitReturnStatement(LuaReturnStatement stat) {
            LuaExpression ret = stat.getReturnValue();

            if (ret instanceof LuaExpressionList) {
                myType.addPossibleReturn(ret.getLuaType());
            }
        }

        @Override
        public void visitAnonymousFunction(LuaAnonymousFunctionExpression e) {
        }

        @Override
        public void visitFunctionDef(LuaFunctionDefinitionStatement e) {
        }
    }
}
