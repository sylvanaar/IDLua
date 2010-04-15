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

package com.sylvanaar.idea.Lua.formatter.processors;

import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.sylvanaar.idea.Lua.formatter.blocks.LuaBlock;
import com.sylvanaar.idea.Lua.formatter.models.spacing.SpacingTokens;
import com.sylvanaar.idea.Lua.parser.LuaElementTypes;


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

    public static Spacing getSpacing(LuaBlock child1, LuaBlock child2, CodeStyleSettings settings) {

        ASTNode leftNode = child1.getNode();
        ASTNode rightNode = child2.getNode();

        //Braces Placement
        // For multi-line strings
        if (!child1.getNode().getTextRange().equals(child1.getTextRange()) || !child2.getNode().getTextRange().equals(child2.getTextRange())) {
            return NO_SPACING;
        }

        // For left parentheses in method declarations or calls
        if (LPAREN.equals(rightNode.getElementType()) &&
                rightNode.getPsi().getParent().getNode() != null &&
                FUNCTION_DEFINITION == rightNode.getPsi().getParent().getNode().getElementType()) {
            return NO_SPACING;
        }

        if (PARAMETER_LIST.equals(rightNode.getElementType())) {
            return NO_SPACING;
        }

        if (FUNCTION_DEFINITION == rightNode.getElementType()) {
            return Spacing.createSpacing(0, 0, settings.BLANK_LINES_AROUND_METHOD + 1, settings.KEEP_LINE_BREAKS, 100);
        }


        if ((PUNCTUATION_SIGNS.contains(rightNode.getElementType())) ||
                (COLON.equals(rightNode.getElementType()))) {
            return NO_SPACING;
        }

        return COMMON_SPACING;
    }

}