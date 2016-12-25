package com.sylvanaar.idea.Lua.editor.completion.smartEnter.fixers;

import com.intellij.lang.ASTNode;
import com.intellij.lang.SmartEnterProcessorWithFixers;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.editor.completion.smartEnter.LuaSmartEnterProcessor;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaConditionalExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaIfThenStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaWhileStatement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by jon on 12/25/2016.
 */
public class LuaWhileConditionFixer extends SmartEnterProcessorWithFixers.Fixer<LuaSmartEnterProcessor> {
    @Override
    public void apply(@NotNull Editor editor, @NotNull LuaSmartEnterProcessor processor, @NotNull PsiElement psiElement) {
        if (psiElement instanceof LuaWhileStatement) {
            final Document doc = editor.getDocument();
            final LuaWhileStatement whileStatement = (LuaWhileStatement) psiElement;
            final LuaConditionalExpression condition = whileStatement.getCondition();
            final ASTNode doKeyword = whileStatement.getNode().findChildByType(LuaElementTypes.DO);

            if (condition.getOperand() == null) {
                if (doKeyword == null) {
                    int stopOffset = doc.getLineEndOffset(doc.getLineNumber(whileStatement.getTextRange().getStartOffset()));
                    final LuaBlock then = whileStatement.getBlock();
                    if (then != null) {
                        stopOffset = Math.min(stopOffset, then.getTextRange().getStartOffset());
                    }
                    stopOffset = Math.min(stopOffset, whileStatement.getTextRange().getEndOffset());

                    doc.replaceString(whileStatement.getTextRange().getStartOffset(), stopOffset, "while  do");

                    processor.registerUnresolvedError(condition.getTextRange().getStartOffset()-1);
                } else {
                    processor.registerUnresolvedError(condition.getTextRange().getStartOffset()-1);
                }
            } else if (doKeyword == null) {
                doc.insertString(condition.getTextRange().getEndOffset(), " do");
            }
        }
    }
}