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

package com.sylvanaar.idea.Lua;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.sylvanaar.idea.Lua.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.parser.LuaElementTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 2:54:53 PM
 */
public class LuaFoldingBuilder implements FoldingBuilder {
    @NotNull
    @Override
        public FoldingDescriptor[] buildFoldRegions(ASTNode node, Document document) {
           List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
           appendDescriptors(node, document, descriptors);
           return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
         } 
         private void appendDescriptors(final ASTNode node, final Document document, final List<FoldingDescriptor> descriptors) {
          if (node.getElementType() == LuaElementTypes.FUNCTION_BLOCK ||
                  node.getElementType() == LuaElementTypes.ANON_FUNCTION_BLOCK)
            descriptors.add(new FoldingDescriptor(node,
                    new TextRange(node.getFirstChildNode().getTextRange().getEndOffset(),
                            node.getTextRange().getEndOffset())));

//           if (LuaElementTypes.FOLDABLE_BLOCKS.contains(node.getElementType())) { 
//             descriptors.add(new FoldingDescriptor(node, node.getTextRange()));
//           }
           if (node.getElementType() == LuaTokenTypes.LONGCOMMENT) {
             descriptors.add(new FoldingDescriptor(node, node.getTextRange()));
           }

           ASTNode child = node.getFirstChildNode();
           while (child != null) {
             appendDescriptors(child, document, descriptors);
             child = child.getTreeNext();
           }
         }

    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        if (node.getElementType() == LuaTokenTypes.LONGCOMMENT)
            return "comment";



        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
