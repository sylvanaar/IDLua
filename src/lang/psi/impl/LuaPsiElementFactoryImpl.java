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
import com.sylvanaar.idea.Lua.debugger.LuaCodeFragment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocParameterReference;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocReferenceElement;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocTag;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;


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

    @Override
    public LuaExpression createExpressionFromText(String newExpression) {
        LuaPsiFile file = createDummyFile("return " + newExpression);

        LuaReturnStatement ret = (LuaReturnStatement) file.getStatements()[0];

        LuaExpressionList exprs = (LuaExpressionList) ret.getReturnValue();

        return exprs.getLuaExpressions().get(0);
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

    public PsiElement createWhiteSpaceFromText(String text) {
        LuaPsiFile file = createDummyFile(text);

        return file.getChildren()[0];
    }

    private LuaPsiFile createDummyFile(String s, boolean isPhisical) {
        return (LuaPsiFile) PsiFileFactory.getInstance(myProject)
                                          .createFileFromText("DUMMY__." +
            LuaFileType.LUA_FILE_TYPE.getDefaultExtension(),
            LuaFileType.LUA_FILE_TYPE, s, System.currentTimeMillis(), isPhisical);
    }

    private LuaPsiFile createDummyFile(String s) {
        return createDummyFile(s, false);
    }

    public PsiFile createLuaFile(String idText) {
        return createLuaFile(idText, false, null);
    }

    public LuaPsiFile createLuaFile(String idText, boolean isPhisical,
        PsiElement context) {
        LuaPsiFile file = createDummyFile(idText, isPhisical);

        //file.setContext(context);
        return file;
    }

    //    public static ASTNode createLocalNameIdentifier(Project project, String name) {
    //                return null;  //To change body of created methods use File | Settings | File Templates.
    //    }
    @Override
    public LuaSymbol createReferenceNameFromText(String newElementName) {
        LuaPsiFile file = createDummyFile(newElementName + " = nil");

        if (! (file.getFirstChild() instanceof LuaAssignmentStatement) ) return null;

        LuaAssignmentStatement assign = (LuaAssignmentStatement) file.getFirstChild();

        assert assign != null;
        if (assign.getLeftExprs().count()!=1) return null;

        LuaSymbol e = assign.getLeftExprs().getSymbols()[0];

        if (e.getText().equals(newElementName))
            return e;

        return null;
    }

    @Override
    public LuaDeclarationExpression createLocalNameIdentifierDecl(String name) {
        LuaPsiFile file = createDummyFile("local " + name);

        final LuaLocalDefinitionStatement expressionStatement = (LuaLocalDefinitionStatement) file.getFirstChild();
        final LuaDeclarationExpression declaration = expressionStatement.getDeclarations()[0];

        return declaration;
    }

    public LuaIdentifier createLocalNameIdentifier(String name) {
        int firstDot = name.indexOf('.');
        String prefix = name.substring(0, firstDot>0?firstDot:name.length());
        LuaPsiFile file = createDummyFile("local " + prefix + "; " + name +
                " = nil");

        final LuaAssignmentStatement expressionStatement = (LuaAssignmentStatement) file.getStatements()[1];
        final LuaReferenceElement ref = (LuaReferenceElement) expressionStatement.getLeftExprs().getFirstChild();

        return (LuaIdentifier) ref.getElement();
    }


    public LuaDeclarationExpression createGlobalNameIdentifierDecl(String name) {
        LuaPsiFile file = createDummyFile(name + "=true");

        final LuaAssignmentStatement expressionStatement = (LuaAssignmentStatement) file.getFirstChild();
        final LuaDeclarationExpression declaration =
                (LuaDeclarationExpression) expressionStatement.getLeftExprs().getFirstChild().getFirstChild();

        return declaration;
    }

    @Override
    public LuaDeclarationExpression createParameterNameIdentifier(String name) {
        LuaPsiFile file = createDummyFile("function a("+name+") end");

        final LuaFunctionDefinitionStatement functionDef = (LuaFunctionDefinitionStatement) file.getFirstChild();

        assert functionDef != null;

        return functionDef.getParameters().getLuaParameters()[0];
    }

    @Override
    public LuaExpressionCodeFragment createExpressionCodeFragment(String text, LuaPsiElement context, boolean b) {
        //LuaPsiFile file = createDummyFile(text);
        LuaCodeFragment file = new LuaCodeFragment(myProject, text);

        return (LuaExpressionCodeFragment) file;
    }

    @Override
    public LuaDocComment createDocCommentFromText(String s) {
        LuaPsiFile file = createDummyFile(s);

        PsiElement e = file.getFirstChild();

        assert e instanceof LuaDocComment;

        return (LuaDocComment) e;
    }

    @Override
    public LuaDocReferenceElement createDocFieldReferenceNameFromText(String elementName) {
        LuaPsiFile file = createDummyFile("--- @field " + elementName + "\nlocal a={" + elementName + "=true}");

        LuaDocComment comment = (LuaDocComment) file.getFirstChild();

        assert comment != null;
        LuaDocTag tag = comment.getTags()[0];
        
        return tag.getDocFieldReference();
    }

    @Override
    public LuaDocParameterReference createParameterDocMemberReferenceNameFromText(String elementName) {
        LuaPsiFile file = createDummyFile("--- @param " + elementName + "\nfunction(" + elementName + ")");

        LuaDocComment comment = (LuaDocComment) file.getFirstChild();

        assert comment != null;
        LuaDocTag tag = comment.getTags()[0];

        return tag.getDocParameterReference();
    }

    public LuaIdentifier createGlobalNameIdentifier(String name) {
        LuaPsiFile file = createDummyFile(name + "=true; nop=" + name);

        final LuaAssignmentStatement expressionStatement = (LuaAssignmentStatement) file.getStatements()[1];
        final LuaReferenceElement ref = (LuaReferenceElement) expressionStatement.getRightExprs().getFirstChild();

        return (LuaIdentifier) ref.getElement();
    }

    @Override
    public LuaIdentifier createFieldNameIdentifier(String name) {
        LuaPsiFile file = createDummyFile("a."+name+"=nil");

        LuaAssignmentStatement assign = (LuaAssignmentStatement) file.getFirstChild();

        assert assign != null;
        LuaReferenceElement element = assign.getLeftExprs().getReferenceExprs()[0];
        LuaCompoundIdentifier id = (LuaCompoundIdentifier) element.getElement();

        return (LuaIdentifier) id.getRightSymbol();
    }
}
