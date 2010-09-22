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

package com.sylvanaar.idea.Lua.lang.formatter.blocks;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.formatter.processors.LuaIndentProcessor;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFileBase;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to generate myBlock hierarchy
 *
 * @author ilyas
 */
public class LuaBlockGenerator implements LuaElementTypes {
    public static final Logger LOG = Logger.getInstance("#Lua.LuaBlockGenerator");
//  private static final TokenSet NESTED = TokenSet.create(REFERENCE_EXPRESSION,
//      PATH_INDEX_PROPERTY,
//      PATH_METHOD_CALL,
//      PATH_PROPERTY_REFERENCE);

    static int level = 0;
    public static List<Block> generateSubBlocks(ASTNode node,
                                                Alignment myAlignment,
                                                Wrap myWrap,
                                                CodeStyleSettings mySettings,
                                                com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock block) {
        //For binary expressions
        PsiElement blockPsi = block.getNode().getPsi();
        if (blockPsi instanceof LuaBinaryExpression &&
                !(blockPsi.getParent() instanceof LuaBinaryExpression)) {
            return generateForBinaryExpr(node, myWrap, mySettings);
        }

        if (blockPsi instanceof LuaKeyValueInitializer) {
            final ArrayList<Block> subBlocks = new ArrayList<Block>();

            ASTNode[] children2 = node.getChildren(null);
            ASTNode[] children = Arrays.copyOfRange(children2, 1, children2.length-1);

            subBlocks.add(new LuaBlock(children2[0], null, Indent.getNoneIndent(), myWrap, mySettings));
            final Alignment alignment = mustAlign(blockPsi, mySettings) ? Alignment.createAlignment() : null;
            for (ASTNode childNode : children) {
                if (canBeCorrectBlock(childNode)) {
                    final Indent indent = Indent.getNormalIndent();
                    subBlocks.add(new LuaBlock(childNode, isKeyword(childNode) ? null : alignment, indent, myWrap, mySettings));
                }
            }            
            subBlocks.add(new LuaBlock(children2[children2.length-1], null, Indent.getNoneIndent(), myWrap, mySettings));

            return subBlocks;
        }

        //For table constructors
        blockPsi = block.getNode().getPsi();
        if (blockPsi instanceof LuaTableConstructor) {
            LuaTableConstructor t = (LuaTableConstructor) blockPsi;
            LOG.info(">> table: " + node);

            final ArrayList<Block> subBlocks = new ArrayList<Block>();



            ASTNode[] children2 = node.getChildren(null);
            ASTNode[] children = Arrays.copyOfRange(children2, 1, children2.length-1);

            subBlocks.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(children2[0], null, Indent.getNoneIndent(), myWrap, mySettings));

            ASTNode prevChildNode = null;
            final Alignment alignment = mustAlign(blockPsi, mySettings) ? Alignment.createAlignment() : null;
            for (ASTNode childNode : children) {
                if (canBeCorrectBlock(childNode)) {
                    final Indent indent = Indent.getNormalIndent();
                    subBlocks.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(childNode, isKeyword(childNode) ? null : alignment, indent, myWrap, mySettings));
                    prevChildNode = childNode;
                }
            }

            subBlocks.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(children2[children2.length-1], null, Indent.getNoneIndent(), myWrap, mySettings));


            LOG.info("<< table: " + node);
            return subBlocks;
        }
        //For multiline strings
//    if ((block.getNode().getElementType() == STRING ||
//        block.getNode().getElementType() == LONGSTRING) &&
//        block.getTextRange().equals(block.getNode().getTextRange())) {
//      String text = block.getNode().getText();
//      if (text.length() > 6) {
//        if (text.substring(0, 3).equals("'''") && text.substring(text.length() - 3).equals("'''") ||
//            text.substring(0, 3).equals("\"\"\"") & text.substring(text.length() - 3).equals("\"\"\"")) {
//          return generateForMultiLineString(block.getNode(), myAlignment, myWrap, mySettings);
//        }
//      }
//    }

//    if (block.getNode().getElementType() == LONGSTRING_BEGIN &&
//        block.getTextRange().equals(block.getNode().getTextRange())) {
//      String text = block.getNode().getText();
//      if (text.length() > 3) {
//        if (text.substring(0, 3).equals("\"\"\"")) {
//          return generateForMultiLineGStringBegin(block.getNode(), myAlignment, myWrap, mySettings);
//        }
//      }
//
//    }


        // For Parameter lists
        if (isListLikeClause(blockPsi)) {
            LOG.info(">> list: " + blockPsi);
            final ArrayList<Block> subBlocks = new ArrayList<Block>();
            ASTNode[] children = node.getChildren(null);
            ASTNode prevChildNode = null;
            final Alignment alignment = mustAlign(blockPsi, mySettings) ? Alignment.createAlignment() : null;
            for (ASTNode childNode : children) {
                if (canBeCorrectBlock(childNode)) {
                    final Indent indent = LuaIndentProcessor.getChildIndent(block, prevChildNode, childNode);
                    subBlocks.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(childNode, isKeyword(childNode) ? null : alignment, indent, myWrap, mySettings));
                    prevChildNode = childNode;
                }
            }
            LOG.info("<< list: " + blockPsi);
            return subBlocks;
        }

        LOG.info(">> parent: " + blockPsi + ": " + node);
        // For other cases
        final ArrayList<Block> subBlocks = new ArrayList<Block>();
        ASTNode[] children = getLuaChildren(node);
        ASTNode prevChildNode = null;
        for (ASTNode childNode : children) {
            if (canBeCorrectBlock(childNode)) {
                final Indent indent = LuaIndentProcessor.getChildIndent(block, prevChildNode, childNode);
                LOG.info("" + level + "     child: " + childNode + "indent " + indent);
                level++;
                subBlocks.add(
                        new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(childNode,
                                blockPsi instanceof LuaBlock ? null : myAlignment,
                                indent, myWrap, mySettings));
                --level;
                prevChildNode = childNode;
            }
        }
        LOG.info("<< parent: " + blockPsi+ ": " + node);
        return subBlocks;
    }

    private static boolean mustAlign(PsiElement blockPsi, CodeStyleSettings mySettings) {
        return true; //mySettings.ALIGN_MULTILINE_PARAMETERS; /* ||
//        blockPsi instanceof GrExtendsClause && mySettings.ALIGN_MULTILINE_EXTENDS_LIST ||
//        blockPsi instanceof GrThrowsClause && mySettings.ALIGN_MULTILINE_THROWS_LIST ||
//        blockPsi instanceof GrConditionalExpression && mySettings.ALIGN_MULTILINE_TERNARY_OPERATION ||
//        blockPsi instanceof GrArgumentList && mySettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS;*/
    }

    private static boolean isListLikeClause(PsiElement blockPsi) {
        return blockPsi instanceof LuaParameterList ||
                blockPsi instanceof LuaIdentifierList ||
                blockPsi instanceof LuaExpressionList;
    }
//

    private static boolean isKeyword(ASTNode node) {
        return node != null && (LuaTokenTypes.KEYWORDS.contains(node.getElementType()) ||
                LuaTokenTypes.BRACES.contains(node.getElementType()));
    }
//
//


    /**
     * @param node Tree node
     * @return true, if the current node can be myBlock node, else otherwise
     */
    private static boolean canBeCorrectBlock(final ASTNode node) {
        return (node.getText().trim().length() > 0);
    }


    private static ASTNode[] getLuaChildren(final ASTNode node) {
        PsiElement psi = node.getPsi();
        if (psi instanceof OuterLanguageElement) {
            TextRange range = node.getTextRange();
            ArrayList<ASTNode> childList = new ArrayList<ASTNode>();
            PsiFile LuaFile = psi.getContainingFile().getViewProvider().getPsi(LuaFileType.LUA_LANGUAGE);
            if (LuaFile instanceof LuaPsiFileBase) {
                addChildNodes(LuaFile, childList, range);
            }
            return childList.toArray(new ASTNode[childList.size()]);
        }
        return node.getChildren(null);
    }

    private static void addChildNodes(PsiElement elem, ArrayList<ASTNode> childNodes, TextRange range) {
        ASTNode node = elem.getNode();
        if (range.contains(elem.getTextRange()) && node != null) {
            childNodes.add(node);
        } else {
            for (PsiElement child : elem.getChildren()) {
                addChildNodes(child, childNodes, range);
            }
        }

    }

    /**
     * Generates blocks for binary expressions
     *
     * @param node
     * @return
     */
    private static List<Block> generateForBinaryExpr(final ASTNode node, Wrap myWrap, CodeStyleSettings mySettings) {
        final ArrayList<Block> subBlocks = new ArrayList<Block>();
        Alignment alignment = mySettings.ALIGN_MULTILINE_BINARY_OPERATION ? Alignment.createAlignment() : null;
        LuaBinaryExpression myExpr = (LuaBinaryExpression) node.getPsi();
        ASTNode[] children = node.getChildren(null);
        if (myExpr.getLeftExpression() instanceof LuaBinaryExpression) {
            addBinaryChildrenRecursively(myExpr.getLeftExpression(), subBlocks, Indent.getContinuationWithoutFirstIndent(), alignment, myWrap, mySettings);
        }
        for (ASTNode childNode : children) {
            if (canBeCorrectBlock(childNode) &&
                    !(childNode.getPsi() instanceof LuaBinaryExpression)) {
                subBlocks.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(childNode, alignment, Indent.getContinuationWithoutFirstIndent(), myWrap, mySettings));
            }
        }
        if (myExpr.getRightExpression() instanceof LuaBinaryExpression) {
            addBinaryChildrenRecursively(myExpr.getRightExpression(), subBlocks, Indent.getContinuationWithoutFirstIndent(), alignment, myWrap, mySettings);
        }
        return subBlocks;
    }

    /**
     * Adds all children of specified element to given list
     *
     * @param elem
     * @param list
     * @param indent
     * @param alignment
     */
    private static void addBinaryChildrenRecursively(PsiElement elem,
                                                     List<Block> list,
                                                     Indent indent,
                                                     Alignment alignment, Wrap myWrap, CodeStyleSettings mySettings) {
        if (elem == null) return;
        ASTNode[] children = elem.getNode().getChildren(null);
        // For binary expressions
        if ((elem instanceof LuaBinaryExpression)) {
            LuaBinaryExpression myExpr = ((LuaBinaryExpression) elem);
            if (myExpr.getLeftExpression() instanceof LuaBinaryExpression) {
                addBinaryChildrenRecursively(myExpr.getLeftExpression(), list, Indent.getContinuationWithoutFirstIndent(), alignment, myWrap, mySettings);
            }
            for (ASTNode childNode : children) {
                if (canBeCorrectBlock(childNode) &&
                        !(childNode.getPsi() instanceof LuaBinaryExpression)) {
                    list.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(childNode, alignment, indent, myWrap, mySettings));
                }
            }
            if (myExpr.getRightExpression() instanceof LuaBinaryExpression) {
                addBinaryChildrenRecursively(myExpr.getRightExpression(), list, Indent.getContinuationWithoutFirstIndent(), alignment, myWrap, mySettings);
            }
        }
    }


    /**
     * Generates blocks for nested expressions like a.b.c etc.
     *
     * @param node
     * @return
     */
    private static List<Block> generateForNestedExpr(final ASTNode node, Alignment myAlignment, Wrap myWrap, CodeStyleSettings mySettings) {
        final ArrayList<Block> subBlocks = new ArrayList<Block>();
        ASTNode children[] = node.getChildren(null);
        if (children.length > 0 && false /* NESTED.contains(children[0].getElementType()) */) {
            addNestedChildrenRecursively(children[0].getPsi(), subBlocks, myAlignment, myWrap, mySettings);
        } else if (canBeCorrectBlock(children[0])) {
            subBlocks.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(children[0], myAlignment, Indent.getContinuationWithoutFirstIndent(), myWrap, mySettings));
        }
        if (children.length > 1) {
            for (ASTNode childNode : children) {
                if (canBeCorrectBlock(childNode) &&
                        children[0] != childNode) {
                    subBlocks.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(childNode, myAlignment, Indent.getContinuationWithoutFirstIndent(), myWrap, mySettings));
                }
            }
        }
        return subBlocks;
    }

    /**
     * Adds nested children for paths
     *
     * @param elem
     * @param list
     * @param myAlignment
     * @param mySettings
     */
    private static void addNestedChildrenRecursively(PsiElement elem,
                                                     List<Block> list, Alignment myAlignment, Wrap myWrap, CodeStyleSettings mySettings) {
        ASTNode[] children = elem.getNode().getChildren(null);
        // For path expressions
        if (children.length > 0 && false /*NESTED.contains(children[0].getElementType())*/) {
            addNestedChildrenRecursively(children[0].getPsi(), list, myAlignment, myWrap, mySettings);
        } else if (canBeCorrectBlock(children[0])) {
            list.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(children[0], myAlignment, Indent.getContinuationWithoutFirstIndent(), myWrap, mySettings));
        }
        if (children.length > 1) {
            for (ASTNode childNode : children) {
                if (canBeCorrectBlock(childNode) &&
                        children[0] != childNode) {
                    if (elem.getNode() != null && false /*
              NESTED.contains(elem.getNode().getElementType())*/) {
                        list.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(childNode, myAlignment, Indent.getContinuationWithoutFirstIndent(), myWrap, mySettings));
                    } else {
                        list.add(new com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaBlock(childNode, myAlignment, Indent.getNoneIndent(), myWrap, mySettings));
                    }
                }
            }
        }
    }

}
