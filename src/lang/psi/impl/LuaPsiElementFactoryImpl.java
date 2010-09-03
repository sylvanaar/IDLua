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

package com.sylvanaar.idea.Lua.lang.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElementFactory;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 14, 2010
 * Time: 7:16:01 PM
 */
public class LuaPsiElementFactoryImpl extends LuaPsiElementFactory {
    Project myProject;

    public LuaPsiElementFactoryImpl(Project project) {
        myProject = project;
    }

    


    public PsiElement createReferenceNameFromText(String refName) {
//        PsiFile file = createLuaFile("a." + refName);
//        LuaStatementElement statement = ((LuaPsiFileBase) file).getStatements()[0];
//        if (!(statement instanceof LuaReferenceExpression)) return null;
//        final PsiElement element = ((LuaReferenceExpression) statement).getReferenceNameElement();
//        if (element == null) {
//            throw new IncorrectOperationException("Incorrect reference name: " + refName);
//        }
//        return element;

        return null;
    }

    @Override
    public LuaExpression createExpressionFromText(String newExpression) {
        LuaPsiFile file = createDummyFile("return " + newExpression);

        LuaReturnStatement ret = (LuaReturnStatement) file.getStatements()[0];

        return (LuaExpression) ret.getReturnValue();
    }

    @Override
    public LuaStatementElement createStatementFromText(String newStatement) {
        LuaPsiFile file = createDummyFile(newStatement);
        return file.getStatements()[0];
    }

    @Override
    public PsiComment createCommentFromText(String s, PsiElement parent) {
        LuaPsiFile file = createDummyFile(s);

        return (PsiComment) file.getChildren()[0];
    }

    public  PsiElement createWhiteSpaceFromText(String text){
        LuaPsiFile file = createDummyFile(text);

        return file.getChildren()[0];
    }


    private LuaPsiFile createDummyFile(String s, boolean isPhisical) {
        return (LuaPsiFile) PsiFileFactory.getInstance(myProject).createFileFromText("DUMMY__." + LuaFileType.LUA_FILE_TYPE.getDefaultExtension(),
                LuaFileType.LUA_FILE_TYPE, s, System.currentTimeMillis(), isPhisical);
    }

    private LuaPsiFile createDummyFile(String s) {
        return createDummyFile(s, false);
    }

    public PsiFile createLuaFile(String idText) {
        return createLuaFile(idText, false, null);
    }

    public LuaPsiFile createLuaFile(String idText, boolean isPhisical, PsiElement context) {
        LuaPsiFile file = createDummyFile(idText, isPhisical);
        //file.setContext(context);
        return file;
    }


}