/*
 * Copyright 2009 Max Ishchenko
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

package com.sylvanaar.idea.Lua;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.psi.LuaDirective;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 13.07.2009
 * Time: 22:00:02
 */

public class LuaFoldingBuilder implements FoldingBuilder {

    public FoldingDescriptor[] buildFoldRegions(ASTNode node, Document document) {

        List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
        doAppend(node, document, descriptors);
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private static ASTNode doAppend(final ASTNode node, final Document document, final List<FoldingDescriptor> descriptors) {

//        if (node.getElementType() == LuaElementTypes.DIRECTIVE) {
//            //let's fold multiline directives only
//            if (document.getLineNumber(node.getStartOffset()) < document.getLineNumber(node.getTextRange().getEndOffset())) {
//                descriptors.add(new FoldingDescriptor(node, node.getTextRange()));
//            }
//        }

        if (node.getElementType() == LuaTokenTypes.FILE) {
            node.getPsi().getFirstChild();
        }

        ASTNode child = node.getFirstChildNode();
        while (child != null) {
            child = doAppend(child, document, descriptors).getTreeNext();
        }

        return node;
    }

    public String getPlaceholderText(ASTNode node) {
       IElementType token = node.getElementType();
       if (token == LuaTokenTypes.LONGCOMMENT)
        return ((LuaDirective) node.getPsi()).getNameString() + " {...}";
       return ((LuaDirective) node.getPsi()).getNameString() + " {...}";
    }

    public boolean isCollapsedByDefault(ASTNode node) {
        return false;
    }


}
