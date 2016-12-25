package com.sylvanaar.idea.Lua.editor.completion.smartEnter.fixers;

import com.intellij.lang.SmartEnterProcessorWithFixers;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.editor.completion.smartEnter.LuaSmartEnterProcessor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDoStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaIfThenStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaWhileStatement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by jon on 12/25/2016.
 */
public class LuaBlockFixer extends SmartEnterProcessorWithFixers.Fixer<LuaSmartEnterProcessor> {
    @Override
    public void apply(@NotNull Editor editor, @NotNull LuaSmartEnterProcessor processor, @NotNull PsiElement element) throws IncorrectOperationException {
        if (element instanceof LuaIfThenStatement ||
                element instanceof LuaDoStatement ||
                element instanceof LuaWhileStatement) {
            final Document doc = editor.getDocument();
            final LuaStatementElement statement = (LuaStatementElement) element;

            if (!statement.getText().endsWith("end")) {
                doc.insertString(statement.getTextRange().getEndOffset(), "\nend");
                editor.getCaretModel().moveToOffset(statement.getTextRange().getEndOffset());
            }
        }
    }
}
