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

package com.sylvanaar.idea.Lua.formatter.blocks;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.templateLanguages.OuterLanguageElement;

import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.formatter.processors.LuaIndentProcessor;
import com.sylvanaar.idea.Lua.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.psi.LuaPsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 14, 2010
 * Time: 1:14:37 AM
 */
public class LuaBlockGenerator {
    private static Alignment myAlignment;
    private static Wrap myWrap;
    private static CodeStyleSettings mySettings;


    public static List<Block> generateSubBlocks(ASTNode node,
                                                Alignment _myAlignment,
                                                Wrap _myWrap,
                                                CodeStyleSettings _mySettings,
                                                LuaBlock block) {
      myWrap = _myWrap;
      mySettings = _mySettings;
      myAlignment = _myAlignment;


//      //For multiline strings
//      if ((block.getNode().getElementType() == mSTRING_LITERAL ||
//          block.getNode().getElementType() == mGSTRING_LITERAL) &&
//          block.getTextRange().equals(block.getNode().getTextRange())) {
//        String text = block.getNode().getText();
//        if (text.length() > 6) {
//          if (text.substring(0, 3).equals("'''") && text.substring(text.length() - 3).equals("'''") ||
//              text.substring(0, 3).equals("\"\"\"") & text.substring(text.length() - 3).equals("\"\"\"")) {
//            return generateForMultiLineString(block.getNode());
//          }
//        }
//      }


      // For other cases
      final ArrayList<Block> subBlocks = new ArrayList<Block>();
      ASTNode children[] = getLuaChildren(node);
      ASTNode prevChildNode = null;
      for (ASTNode childNode : children) {
        if (canBeCorrectBlock(childNode)) {
          final Indent indent = LuaIndentProcessor.getChildIndent(block, prevChildNode, childNode);
          subBlocks.add(new LuaBlock(childNode, myAlignment, indent, myWrap, mySettings));
          prevChildNode = childNode;
        }
      }
      return subBlocks;
    }

//    private static boolean mustAlign(PsiElement blockPsi) {
//      return blockPsi instanceof GrParameterList && mySettings.ALIGN_MULTILINE_PARAMETERS ||
//          blockPsi instanceof GrExtendsClause && mySettings.ALIGN_MULTILINE_EXTENDS_LIST ||
//          blockPsi instanceof GrThrowsClause && mySettings.ALIGN_MULTILINE_THROWS_LIST ||
//          blockPsi instanceof GrConditionalExpression && mySettings.ALIGN_MULTILINE_TERNARY_OPERATION ||
//          blockPsi instanceof GrArgumentList && mySettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS;
//    }
//
//    private static boolean isListLikeClause(PsiElement blockPsi) {
//      return blockPsi instanceof GrParameterList ||
//          blockPsi instanceof GrArgumentList ||
//          blockPsi instanceof GrConditionalExpression ||
//          blockPsi instanceof GrExtendsClause ||
//          blockPsi instanceof GrThrowsClause;
//    }

    private static boolean isKeyword(ASTNode node) {
      return node != null && (LuaTokenTypes.KEYWORDS.contains(node.getElementType()));
    }


    private static List<Block> generateForMultiLineString(ASTNode node) {
      final ArrayList<Block> subBlocks = new ArrayList<Block>();
      final int start = node.getTextRange().getStartOffset();
      final int end = node.getTextRange().getEndOffset();

      subBlocks.add(new LuaBlock(node, myAlignment, Indent.getNoneIndent(), myWrap, mySettings) {
        @NotNull
        public TextRange getTextRange() {
          return new TextRange(start, start + 3);
        }
      });
      subBlocks.add(new LuaBlock(node, myAlignment, Indent.getAbsoluteNoneIndent(), myWrap, mySettings) {
        @NotNull
        public TextRange getTextRange() {
          return new TextRange(start + 3, end - 3);
        }
      });
      subBlocks.add(new LuaBlock(node, myAlignment, Indent.getAbsoluteNoneIndent(), myWrap, mySettings) {
        @NotNull
        public TextRange getTextRange() {
          return new TextRange(end - 3, end);
        }
      });
      return subBlocks;
    }



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
        if (LuaFile instanceof LuaPsiFile) {
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
}
