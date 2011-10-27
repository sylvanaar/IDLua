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

package com.sylvanaar.idea.Lua.lang.formatter.processors;

import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaFormattingBlock;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaTableConstructor;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaFunctionArguments;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



public abstract class LuaIndentProcessor implements LuaElementTypes {
 
  /**
   * Calculates indent, based on code style, between parent block and child node
   *
   * @param parent        parent block
   * @param child         child node
   * @param prevChildNode previous child node
   * @return indent
   */
  @NotNull
  public static Indent getChildIndent(@NotNull final LuaFormattingBlock parent, @Nullable final ASTNode prevChildNode, @NotNull final ASTNode child) {
    ASTNode astNode = parent.getNode();
    final PsiElement psiParent = astNode.getPsi();


//    if (psiParent instanceof LuaAnonymousFunctionExpression)
//        return Indent.getNormalIndent();
      
    // For Lua Blocks
    if (child.getPsi() instanceof LuaBlock) {
        return Indent.getNormalIndent();
    }



    if (psiParent instanceof LuaTableConstructor) {
        if (child.getElementType() == RCURLY)
            return Indent.getNoneIndent();
        if (child.getElementType() == LCURLY)
            return Indent.getContinuationWithoutFirstIndent();

        return Indent.getNormalIndent();
    }


    if ((child.getElementType() == LUADOC_COMMENT || child.getPsi() instanceof PsiComment) && psiParent instanceof LuaStatementElement) {
        return Indent.getNormalIndent();
    }

    if (psiParent.getParent() instanceof LuaFunctionArguments) {
        if (child.getElementType() == RPAREN)
            return Indent.getNoneIndent();
        if (child.getElementType() == LPAREN)
            return Indent.getContinuationWithoutFirstIndent();

        return Indent.getNormalIndent();
    }
    // For common code block
//    if (BLOCK_SET.contains(astNode.getElementType())) {
//      return indentForBlock(psiParent, child);
//    }

    return Indent.getNoneIndent();
  }


//  /**
//   * Indent for common block
//   *
//   * @param psiBlock
//   * @param child
//   * @return
//   */
//  private static Indent indentForBlock(PsiElement psiBlock, ASTNode child) {
//    // Common case
//    if (BLOCK_BEGIN_SET.contains(child.getElementType()) ||
//        BLOCK_END_SET.contains(child.getElementType())) {
//      return Indent.getNoneIndent();
//    }
//    return Indent.getNormalIndent();
//
//
//  }
}

