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
package com.sylvanaar.idea.Lua.intentions.comments;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.intentions.base.Intention;
import com.sylvanaar.idea.Lua.intentions.base.PsiElementPredicate;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElementFactory;
import org.jetbrains.annotations.NotNull;


public class ChangeToEndOfLineCommentIntention extends Intention {

    @NotNull
    protected PsiElementPredicate getElementPredicate() {
        return new CStyleCommentPredicate();
    }

    public void processIntention(@NotNull PsiElement element)
            throws IncorrectOperationException {
        final PsiComment comment = (PsiComment) element;
        final LuaPsiElementFactory factory = LuaPsiElementFactory.getInstance(comment.getProject());
        final PsiElement parent = comment.getParent();
        assert parent != null;
        // final PsiElementFactory factory = manager.getElementFactory();
        final String commentText = comment.getText();
        final PsiElement whitespace = comment.getNextSibling();
        int b1 = commentText.indexOf('[');
        int b2 = commentText.indexOf('[', b1 + 1);

        String text = commentText.substring(b2 + 1, commentText.length() - (b2 - b1 + 1));
        if (text.charAt(0) == '\n')
            text = text.substring(1);
        if (text.charAt(text.length()-1) == '\n')
            text = text.substring(0,text.length()-1);

        final String[] lines = text.split("\n");
        int i;
        for (i = lines.length - 1; i >= 1; i--) {
            final PsiComment nextComment =
                    factory.createCommentFromText("-- " + lines[i].trim() + '\n',
                            parent);
            parent.addAfter(nextComment, comment);
            if (whitespace != null) {
                final PsiElement newWhiteSpace =
                        factory.createWhiteSpaceFromText(whitespace.getText());
                parent.addAfter(newWhiteSpace, comment);
            }
        }
        
        final PsiComment newComment =
                factory.createCommentFromText("-- " + lines[0].trim(), parent);
        comment.replace(newComment);
    }
}
