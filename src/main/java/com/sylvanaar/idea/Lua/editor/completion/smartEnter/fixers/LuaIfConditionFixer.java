package com.sylvanaar.idea.Lua.editor.completion.smartEnter.fixers;

import com.intellij.lang.ASTNode;
import com.intellij.lang.SmartEnterProcessorWithFixers;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.editor.completion.smartEnter.LuaSmartEnterProcessor;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaIfThenStatement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by jon on 12/25/2016.
 */
public class LuaIfConditionFixer extends SmartEnterProcessorWithFixers.Fixer<LuaSmartEnterProcessor> {
    @Override
    public void apply(@NotNull Editor editor, @NotNull LuaSmartEnterProcessor processor, @NotNull PsiElement psiElement) {
        if (psiElement instanceof LuaIfThenStatement) {
            final Document doc = editor.getDocument();
            final LuaIfThenStatement ifStatement = (LuaIfThenStatement) psiElement;
            final LuaExpression condition = ifStatement.getIfCondition();
            final ASTNode thenKeyword = ifStatement.getNode().findChildByType(LuaElementTypes.THEN);
            final ASTNode ifKeyword = ifStatement.getNode().findChildByType(LuaElementTypes.IF);

            if (!(condition.getFirstChild() instanceof LuaExpression)) {
                if (thenKeyword == null) {
                    int stopOffset = doc.getLineEndOffset(doc.getLineNumber(ifStatement.getTextRange().getStartOffset()));
                    final LuaBlock then = ifStatement.getIfBlock();
                    if (then != null) {
                        stopOffset = Math.min(stopOffset, then.getTextRange().getStartOffset());
                    }
                    stopOffset = Math.min(stopOffset, ifStatement.getTextRange().getEndOffset());

                    doc.replaceString(ifStatement.getTextRange().getStartOffset(), stopOffset, "if  then");

                    processor.registerUnresolvedError(condition.getTextRange().getStartOffset()-1);
                } else {
                    processor.registerUnresolvedError(condition.getTextRange().getStartOffset()-1);
                }
            } else if (thenKeyword == null) {
                doc.insertString(condition.getTextRange().getEndOffset(), " then");
            }
        }
    }
}