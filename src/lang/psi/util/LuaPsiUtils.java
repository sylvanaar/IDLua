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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * User: jansorg
 * Date: 04.08.2009
 * Time: 21:45:47
 */
public class LuaPsiUtils {
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
        ASTNode next = node != null ? node.getTreeNext() : null;
        return next != null ? next.getPsi() : null;
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
        while (element != null && element.getContext() != null) {
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

    public static boolean processChildDeclarationsS(PsiElement parentContainer, PsiScopeProcessor processor,
                                                    ResolveState resolveState, PsiElement parent, PsiElement place) {
        PsiElement child = parentContainer.getFirstChild();
        while (child != null) {
            if (!child.processDeclarations(processor, resolveState, parent, place)) {
                return false;
            }

            child = child.getNextSibling();
        }

        return true;

    }

    public static boolean processChildDeclarations(PsiElement element, PsiScopeProcessor processor,
                                                   ResolveState substitutor, PsiElement lastParent,
                                                   PsiElement place) {
        PsiElement run = lastParent == null ? element.getLastChild() : lastParent.getPrevSibling();
        while (run != null) {
            if (!run.processDeclarations(processor, substitutor, null, place)) return false;
            run = run.getPrevSibling();
        }

        return true;
    }


    public static int getElementLineNumber(PsiElement element) {
        FileViewProvider fileViewProvider = element.getContainingFile().getViewProvider();
        if (fileViewProvider.getDocument() != null) {
            return fileViewProvider.getDocument().getLineNumber(element.getTextOffset()) + 1;
        }

        return 0;
    }

    public static int getElementEndLineNumber(PsiElement element) {
        FileViewProvider fileViewProvider = element.getContainingFile().getViewProvider();
        if (fileViewProvider.getDocument() != null) {
            return fileViewProvider.getDocument().getLineNumber(element.getTextOffset() + element.getTextLength()) +
                    1;
        }

        return 0;
    }

    @Nullable
    public static LuaPsiElement getCoveringPsiElement(@NotNull final PsiElement psiElement) {
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

    public static PsiElement findNextSibling(PsiElement start, IElementType ignoreType) {
        PsiElement current = start.getNextSibling();
        while (current != null) {
            if (ignoreType != nodeType(current)) {
                return current;
            }

            current = current.getNextSibling();
        }

        return null;
    }

    public static PsiElement findPreviousSibling(PsiElement start, IElementType ignoreType) {
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
                original.getNode().replaceAllChildrenToChildrenOf(replacement.getNode());
                return original;
            }
        } finally {

        }
    }



    public static boolean isLValue(LuaPsiElement element) {
      if (element instanceof LuaReferenceElement)
        if (((LuaReferenceElement) element).getElement().getParent().getParent() instanceof LuaIdentifierList)
            return true;

      if (element instanceof LuaSymbol)
        if (element.getParent().getParent() instanceof LuaIdentifierList)
            return true;
      
      return false;
    }


  @NotNull
  public static LuaDocPsiElement[] toPsiElementArray(@NotNull Collection<? extends LuaDocPsiElement> collection) {
    if (collection.isEmpty()) return LuaDocPsiElement.EMPTY_ARRAY;
    return collection.toArray(new LuaDocPsiElement[collection.size()]);
  }
  @NotNull
  public static LuaPsiElement[] toPsiElementArray(@NotNull Collection<? extends LuaPsiElement> collection) {
    if (collection.isEmpty()) return LuaPsiElement.EMPTY_ARRAY;
    return collection.toArray(new LuaPsiElement[collection.size()]);
  }
  @NotNull
  public static PsiElement[] toPsiElementArray(@NotNull Collection<? extends PsiElement> collection) {
    if (collection.isEmpty()) return PsiElement.EMPTY_ARRAY;
    return collection.toArray(new PsiElement[collection.size()]);
  }

}
