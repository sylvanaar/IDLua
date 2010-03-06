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

package com.sylvanaar.idea.Lua.formatter.blocks;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.sylvanaar.idea.Lua.lexer.LuaElementTypes;
import com.sylvanaar.idea.Lua.psi.LuaComplexValue;
import com.sylvanaar.idea.Lua.psi.LuaContext;
import com.sylvanaar.idea.Lua.psi.LuaDirective;
import com.sylvanaar.idea.Lua.psi.LuaPsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 13.07.2009
 * Time: 23:00:41
 */
public class LuaBlock implements ASTBlock {

    private static final Spacing ONE_LINE_BREAK_SPACING = Spacing.createSpacing(0, 0, 1, true, 50);

    private ASTNode node;
    private Indent indent;
    private Alignment alignment;
    private List<Block> blocks;

    public LuaBlock(ASTNode node, Indent indent, Alignment alignment) {
        this.node = node;
        this.indent = indent;
        this.alignment = alignment;
    }

    @NotNull
    public List<Block> getSubBlocks() {

        if (blocks == null) {

            blocks = new ArrayList<Block>();

            boolean isFileNode = getNode().getPsi() instanceof LuaPsiFile;

            for (ASTNode childNode : getNode().getChildren(null)) {

                if (!(childNode.getPsi() instanceof PsiWhiteSpace)) { //just omitting whitespace tokens

                    Indent childIndent = Indent.getNoneIndent();

                    if (!isFileNode && (childNode.getPsi() instanceof LuaDirective || childNode.getPsi() instanceof PsiComment)) {
                        childIndent = Indent.getNormalIndent();
                    }

                    blocks.add(new LuaBlock(childNode, childIndent, null));

                }
            }
        }
        return blocks;
    }

    public Spacing getSpacing(Block genericLeftBlock, Block genericRightBlock) {

        LuaBlock leftBlock = (LuaBlock) genericLeftBlock;
        LuaBlock rightBlock = (LuaBlock) genericRightBlock;

        PsiElement leftPsi = leftBlock.getNode().getPsi();
        PsiElement rightPsi = rightBlock.getNode().getPsi();

        if (rightPsi instanceof LuaDirective) {
            return ONE_LINE_BREAK_SPACING;
        }

        if (rightBlock.getNode().getElementType() == LuaElementTypes.CLOSING_BRACE) {
            return ONE_LINE_BREAK_SPACING;
        }

        if (leftBlock.getNode().getElementType() == LuaElementTypes.OPENING_BRACE) {
            return ONE_LINE_BREAK_SPACING;
        }

        return null;
    }

    public ASTNode getNode() {
        return node;
    }

    @NotNull
    public TextRange getTextRange() {
        return node.getTextRange();
    }

    public Wrap getWrap() {
        return null;
    }

    public Indent getIndent() {
        return indent;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public boolean isIncomplete() {
        return false;
    }

    public boolean isLeaf() {
        return getNode().getFirstChildNode() == null || getNode().getPsi() instanceof LuaComplexValue;
    }

    @NotNull
    public ChildAttributes getChildAttributes(int newChildIndex) {

        if (getNode().getPsi() instanceof LuaContext) {
            return new ChildAttributes(Indent.getNormalIndent(), null);
        }

        return new ChildAttributes(Indent.getNoneIndent(), null);
    }
}
