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

import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaFormattingBlock;
import com.sylvanaar.idea.Lua.lang.formatter.models.spacing.SpacingTokens;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaTableConstructor;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaGenericForStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaNumericForStatement;


/**
 * @author ilyas
 */
public abstract class LuaSpacingProcessorBasic extends SpacingTokens implements LuaElementTypes {

    private static final Spacing NO_SPACING_WITH_NEWLINE = Spacing.createSpacing(0, 0, 0, true, 1);
    private static final Spacing NO_SPACING = Spacing.createSpacing(0, 0, 0, false, 0);
    private static final Spacing COMMON_SPACING = Spacing.createSpacing(1, 1, 0, true, 100);
    private static final Spacing COMMON_SPACING_WITH_NL = Spacing.createSpacing(1, 1, 1, true, 100);
    private static final Spacing IMPORT_BETWEEN_SPACING = Spacing.createSpacing(0, 0, 1, true, 100);
    private static final Spacing IMPORT_OTHER_SPACING = Spacing.createSpacing(0, 0, 2, true, 100);
    private static final Spacing LAZY_SPACING = Spacing.createSpacing(0, 239, 0, true, 100);

    public static Spacing getSpacing(LuaFormattingBlock child1, LuaFormattingBlock child2, CodeStyleSettings settings) {

        ASTNode leftNode = child1.getNode();
        ASTNode rightNode = child2.getNode();

        //Braces Placement
//        // For multi-line strings
//        if (!child1.getNode().getTextRange().equals(child1.getTextRange()) || !child2.getNode().getTextRange().equals(child2.getTextRange())) {
//            return NO_SPACING;
//        }
//
//        // For left parentheses in method declarations or calls
//        if (LPAREN.equals(rightNode.getElementType()) &&
//                rightNode.getPsi().getParent().getNode() != null &&
//                FUNCTION_IDENTIFIER == rightNode.getPsi().getParent().getNode().getElementType()) {
//            return NO_SPACING;
//        }
//

        if (leftNode.getElementType() == UNARY_OP) {
            if (!leftNode.getText().equals("not"))
                return NO_SPACING;
            else
                return COMMON_SPACING;
        }

        if ((leftNode.getElementType() == COMMA || leftNode.getElementType() == ASSIGN || rightNode.getElementType() == ASSIGN) &&
                ( leftNode.getPsi().getContext() instanceof LuaGenericForStatement ||
                        leftNode.getPsi().getContext() instanceof LuaNumericForStatement))
            return NO_SPACING;

        if (rightNode.getElementType() == RBRACK || leftNode.getElementType() == LBRACK || rightNode.getElementType() == LBRACK)
            return NO_SPACING;

        if (rightNode.getElementType() == RPAREN || leftNode.getElementType() == LPAREN || rightNode.getElementType() == LPAREN)
            return NO_SPACING;

        if (PARAMETER_LIST.equals(rightNode.getElementType()) ||
                FUNCTION_CALL_ARGS.equals(rightNode.getElementType()) ||
                ANONYMOUS_FUNCTION_EXPRESSION.equals(rightNode.getElementType())) {
            return NO_SPACING;
        }

        if (FUNCTION_DEFINITION.equals(leftNode.getElementType()))
            return Spacing.createSpacing(1, 1, 2, true, 100);;

        if (rightNode.getElementType() == FIELD_NAME)
            return NO_SPACING;

        if (rightNode.getPsi().getContext() instanceof LuaTableConstructor) {
            if (leftNode.getElementType() == LCURLY) {
                return NO_SPACING_WITH_NEWLINE;
            }
            if (rightNode.getElementType() == RCURLY) {
                return NO_SPACING_WITH_NEWLINE;
            }
        }
        
        if ((PUNCTUATION_SIGNS.contains(rightNode.getElementType())) ||
                (COLON.equals(rightNode.getElementType()))) {
            return NO_SPACING;
        }

        return COMMON_SPACING;
    }

}