/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.intentions.style;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.intentions.LuaIntentionsBundle;
import com.sylvanaar.idea.Lua.intentions.base.IntentionUtils;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaStringLiteralExpressionImpl;
import org.jetbrains.annotations.NotNull;

public class ConvertQuotesIntention extends BaseIntentionAction
{
    @NotNull
    public String getFamilyName() {
        return LuaIntentionsBundle.message("quoted.string.intention.family.name");
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        boolean bResult = false;
        if(file instanceof LuaPsiFile) {
            LuaStringLiteralExpressionImpl stringLiteral = PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), LuaStringLiteralExpressionImpl.class);
            if(stringLiteral != null) {
                final String quoteChar = stringLiteral.getOpenQuote();
                if(quoteChar == null || quoteChar.startsWith("[")) {
                    bResult = false;
                } else if(quoteChar.startsWith("'")) {
                    setText(LuaIntentionsBundle.message("quoted.string.single.to.double"));
                    bResult = true;
                } else if(quoteChar.startsWith("\"")) {
                    setText(LuaIntentionsBundle.message("quoted.string.double.to.single"));
                    bResult = true;
                }
            }
        }
        return bResult;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        LuaStringLiteralExpressionImpl stringLiteral = PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), LuaStringLiteralExpressionImpl.class);
        if(stringLiteral != null) {
            final String quoteChar = stringLiteral.getOpenQuote();
            final String stringText = stringLiteral.getText();

            assert quoteChar != null;
            final char oldQuote = quoteChar.charAt(0);
            final char newQuote = oldQuote == '"' ? '\'' : '"';
            IntentionUtils.replaceExpression(swapQuotes(stringText, oldQuote, newQuote), stringLiteral);
        }
    }

    @NotNull
    private static String swapQuotes(@NotNull String stringText, char oldQuote, char newQuote) {
        boolean skipNext = false;
        String escapedQuote = "\\" + newQuote;
        char[] charArr = stringText.toCharArray();
        int stringLength = charArr.length;
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i != stringLength; ++i) {
            char ch = charArr[i];
            if(skipNext) {
                skipNext = false;
                continue;
            }

            if(ch == oldQuote) {
                stringBuilder.append(newQuote);
            } else if(ch == newQuote) {
                stringBuilder.append(escapedQuote);
            } else if(ch == '\\' && charArr[i + 1] == oldQuote && !(i + 2 == stringLength)) {
                skipNext = true;
                stringBuilder.append(charArr[i + 1]);
            } else {
                stringBuilder.append(ch);
            }
        }

        return stringBuilder.toString();
    }
}
