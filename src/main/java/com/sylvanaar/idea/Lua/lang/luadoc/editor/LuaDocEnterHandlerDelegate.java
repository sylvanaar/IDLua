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

package com.sylvanaar.idea.Lua.lang.luadoc.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.LuaDocElementTypes;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocCommentOwner;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaKeyValueInitializer;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaTableConstructor;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/2/11
 * Time: 6:54 PM
 */
public class LuaDocEnterHandlerDelegate implements EnterHandlerDelegate {
    @Override
    public Result preprocessEnter(PsiFile file, Editor editor, Ref<Integer> caretOffset, Ref<Integer> caretAdvance,
                                  DataContext dataContext, EditorActionHandler originalHandler) {
        if (! (file instanceof LuaPsiFile))
            return Result.Continue;
        
        Document document = editor.getDocument();
        int caret = caretOffset.get();

        PsiElement e1 = file.findElementAt(caret - 1);

        if (e1 != null && e1.getNode().getElementType() == LuaDocElementTypes.LDOC_COMMENT_START) {
            // The user has typed the doc comment start and hit enter.
            // we want to autogenerate the luadocs
            String indent = CodeStyleManager
                    .getInstance(file.getProject()).getLineIndent(file, editor.getCaretModel().getOffset());

            StringBuilder luadoc = new StringBuilder().append(" \n");

            PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);

            e1 = file.findElementAt(caret - 1);
            if (e1 != null && e1.getContext() instanceof LuaDocComment) {
                LuaDocComment comment = (LuaDocComment) e1.getContext();

                assert comment != null;

                LuaDocCommentOwner owner = comment.getOwner();
                if (owner != null) {
                    if (owner instanceof LuaFunctionDefinition) {
                        LuaParameter[] parms = ((LuaFunctionDefinition) owner).getParameters()
                                .getLuaParameters();

                        for (LuaParameter p : parms)
                            luadoc.append(indent).append("-- @param ").append(p.getName()).append(" \n");

                        luadoc.append(indent).append("--");
                    } else if (owner instanceof LuaTableConstructor) {
                        LuaExpression[] initalizers = ((LuaTableConstructor) owner).getInitializers();

                        for (LuaExpression expression : initalizers)
                            if (expression instanceof LuaKeyValueInitializer) {
                                LuaExpression key = ((LuaKeyValueInitializer) expression).getFieldKey();
                                if (key instanceof LuaFieldIdentifier)
                                    luadoc.append(indent).append("-- @field ").append(((LuaFieldIdentifier) key).getName()).append(" \n");
                            }
                            
                        luadoc.append(indent).append("--");
                    }

                    document.insertString(caret, luadoc);

                    editor.getCaretModel().moveCaretRelatively(1, 0, false, false, false);

                    return Result.Stop;
                }
            }
        }

        return Result.Continue;
    }

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
        return Result.Continue;
    }
}
