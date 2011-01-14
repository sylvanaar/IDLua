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

package com.sylvanaar.idea.Lua.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaTableConstructor;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes.LONGCOMMENT;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 2:54:53 PM
 */
public class LuaFoldingBuilder implements FoldingBuilder {
    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
        List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
        appendDescriptors(node, document, descriptors);
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }


    private void appendDescriptors(final ASTNode node, final Document document, final List<FoldingDescriptor> descriptors) {
        try {
            if (isFoldableNode(node)) {
                final PsiElement psiElement = node.getPsi();

                if (psiElement instanceof LuaFunctionDefinitionStatement) {
                    LuaFunctionDefinitionStatement stmt = (LuaFunctionDefinitionStatement) psiElement;
                    
                    descriptors.add(new FoldingDescriptor(node,
                            new TextRange(stmt.getParameters().getTextRange().getEndOffset() + 1,
                                    node.getTextRange().getEndOffset())));
                }

                if (psiElement instanceof LuaTableConstructor) {
                    LuaTableConstructor stmt = (LuaTableConstructor) psiElement;

                    if (stmt.getText().indexOf("\n")>0)
                        descriptors.add(new FoldingDescriptor(node,
                                new TextRange(stmt.getTextRange().getStartOffset() + 1,
                                        node.getTextRange().getEndOffset() - 1)));
                }
            }

            if (node.getElementType() == LONGCOMMENT && node.getTextLength() > 2) {
                descriptors.add(new FoldingDescriptor(node, node.getTextRange()));
            }

            ASTNode child = node.getFirstChildNode();
            while (child != null) {
                appendDescriptors(child, document, descriptors);
                child = child.getTreeNext();
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isFoldableNode(ASTNode node) {
        return node.getElementType() == LuaElementTypes.FUNCTION_DEFINITION ||
                node.getElementType() == LuaElementTypes.TABLE_CONSTUCTOR;
    }

    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        if (node.getElementType() == LONGCOMMENT)
            return "comment";


        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
