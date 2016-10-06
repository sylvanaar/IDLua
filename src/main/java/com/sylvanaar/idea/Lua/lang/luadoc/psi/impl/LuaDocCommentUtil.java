/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

package com.sylvanaar.idea.Lua.lang.luadoc.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocCommentOwner;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocPsiElement;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaMaybeDeclarationAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import org.jetbrains.annotations.Nullable;


public abstract class LuaDocCommentUtil {
    private  static final Logger log = Logger.getInstance("Lua.LuaDocCommentUtil");
    @Nullable
    public static LuaDocCommentOwner findDocOwner(LuaDocPsiElement docElement) {
        PsiElement element = docElement;
        while (element != null && element.getParent() instanceof LuaDocPsiElement) element = element.getParent();
        if (element == null) return null;

        while (true) {
            element = element.getNextSibling();
            if (element instanceof LuaBlock)
                element = element.getFirstChild();
            if (element == null) return null;
            final ASTNode node = element.getNode();
            if (node == null) return null;
            if (LuaElementTypes.LUADOC_COMMENT.equals(node.getElementType()) ||
                !LuaElementTypes.WHITE_SPACES_OR_COMMENTS.contains(node.getElementType())) {
                break;
            }
        }

        if (element instanceof LuaDocCommentOwner) return (LuaDocCommentOwner) element;

        if (element instanceof LuaMaybeDeclarationAssignmentStatement) {
            LuaExpression[] expressions = ((LuaMaybeDeclarationAssignmentStatement) element).getDefinedSymbolValues();

            for (LuaExpression e : expressions)
                if (e instanceof LuaDocCommentOwner) return (LuaDocCommentOwner) e;
        }


        return null;
    }

    @Nullable
    public static LuaDocComment findDocComment(LuaDocCommentOwner owner) {
        PsiElement element = owner;

        if (!(owner instanceof LuaStatementElement))
            element = element.getParent().getParent();

        element = element.getPrevSibling();

        while (true) {
            if (element == null) return null;
            final ASTNode node = element.getNode();
            if (node == null) return null;
            if (!node.getElementType().equals(LuaElementTypes.MAIN_CHUNK_VARARGS))
                if (LuaElementTypes.LUADOC_COMMENT.equals(node.getElementType()) ||
                    !LuaElementTypes.WHITE_SPACES_OR_COMMENTS.contains(node.getElementType())) {
                    break;
                }
            element = element.getPrevSibling();
        }
        if (element instanceof LuaDocComment) return (LuaDocComment) element;
        return null;
    }
}
