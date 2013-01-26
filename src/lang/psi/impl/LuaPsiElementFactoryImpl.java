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
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.debugger.LuaCodeFragment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocParameterReference;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocReferenceElement;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocTag;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.Nullable;


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

        LuaReturnStatement ret = (LuaReturnStatement)  getChildOfFirstStatement(file);

        LuaExpressionList exprs = ret.getReturnValue();

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

    private LuaPsiFile createDummyFile(CharSequence s, boolean isPhysical) {
        return (LuaPsiFile) PsiFileFactory.getInstance(myProject)
                                          .createFileFromText("DUMMY__." +
            LuaFileType.LUA_FILE_TYPE.getDefaultExtension(),
            LuaFileType.LUA_FILE_TYPE, s, System.currentTimeMillis(), isPhysical);
    }

    private LuaPsiFile createDummyFile(String s) {
        return createDummyFile(s, false);
    }

    @Override
    public LuaPsiFile createLuaFile(String text) {
        return createLuaFile(text, false, null);
    }

    public LuaPsiFile createLuaFile(CharSequence text, boolean isPhysical,
        @Nullable PsiElement context) {
        LuaPsiFile file = createDummyFile(text, isPhysical);

//        file.setConte(context);
        return file;
    }

    @Override
    public LuaSymbol createReferenceNameFromText(String newElementName) {
        LuaPsiFile file = createDummyFile(newElementName + " = nil");

        if (PsiTreeUtil.hasErrorElements(file)) return null;

        if (! (getChildOfFirstStatement(file) instanceof LuaAssignmentStatement) ) return null;

        LuaAssignmentStatement assign = (LuaAssignmentStatement) getChildOfFirstStatement(file);

        assert assign != null;
        final LuaIdentifierList leftExprs = assign.getLeftExprs();
        if (leftExprs.count() == 0) return null;

        LuaSymbol e = leftExprs.getSymbols()[0];

        if (e.getText().equals(newElementName))
            return e;

        return null;
    }




    @Override
    public LuaDeclarationExpression createLocalNameIdentifierDecl(String name) {
        LuaPsiFile file = createDummyFile("local " + name + " = 1");
        final LuaDeclarationExpression[] declaration = new LuaDeclarationExpression[1];

        file.acceptChildren(new LuaElementVisitor() {
            @Override
            public void visitDeclarationStatement(LuaDeclarationStatement e) {
                declaration[0] = (LuaDeclarationExpression) e.getDefinedSymbols()[0];
            }
        });
        return declaration[0];
    }

    public LuaIdentifier createLocalNameIdentifier(String name) {
        int firstDot = name.indexOf('.');
        String prefix = name.substring(0, firstDot>0?firstDot:name.length());
        LuaPsiFile file = createDummyFile("local " + prefix + "; " + name +
                " = nil");

        final LuaIdentifier[] declaration = new LuaIdentifier[1];

        file.acceptChildren(new LuaElementVisitor() {
            @Override
            public void visitAssignment(LuaAssignmentStatement e) {
                if (e instanceof LuaLocalDefinitionStatement) return;
                declaration[0] = (LuaIdentifier) e.getAssignments()[0].getSymbol();
            }
        });
        return declaration[0];
    }


    public LuaDeclarationExpression createGlobalNameIdentifierDecl(String name) {
        LuaPsiFile file = createDummyFile(name + "=true");

        final LuaAssignmentStatement expressionStatement = (LuaAssignmentStatement) getChildOfFirstStatement(file);
        final LuaDeclarationExpression declaration =
                (LuaDeclarationExpression) expressionStatement.getLeftExprs().getFirstChild().getFirstChild();

        return declaration;
    }

    @Override
    public LuaDeclarationExpression createParameterNameIdentifier(String name) {
        LuaPsiFile file = createDummyFile("function a("+name+") end");

        final LuaFunctionDefinitionStatement functionDef = (LuaFunctionDefinitionStatement) getChildOfFirstStatement(file);

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

        PsiElement e = getChildOfFirstStatement(file);

        assert e instanceof LuaDocComment;

        return (LuaDocComment) e;
    }

    @Override
    public LuaDocReferenceElement createDocFieldReferenceNameFromText(String elementName) {
        LuaPsiFile file = createDummyFile("--- @field " + elementName + "\nlocal a={" + elementName + "=true}");

        LuaDocComment comment = (LuaDocComment) getChildOfFirstStatement(file);

        assert comment != null;
        LuaDocTag tag = comment.getTags()[0];
        
        return tag.getDocFieldReference();
    }

    @Override
    public LuaDocParameterReference createParameterDocMemberReferenceNameFromText(String elementName) {
        LuaPsiFile file = createDummyFile("--- @param " + elementName + "\nfunction(" + elementName + ")");

        LuaDocComment comment = (LuaDocComment) getChildOfFirstStatement(file);

        assert comment != null;
        LuaDocTag tag = comment.getTags()[0];

        return tag.getDocParameterReference();
    }




    @Override
    public LuaIdentifier createIdentifier(String name) {
        LuaPsiFile file = createDummyFile(name + "=true");
        final LuaIdentifier[] declaration = new LuaIdentifier[1];

        file.accept(new LuaElementVisitor() {
            @Override
            public void visitAssignment(LuaAssignmentStatement e) {
                declaration[0] = (LuaIdentifier) e.getAssignments()[0].getSymbol();
            }
        });
        return declaration[0];
    }

    public LuaIdentifier createGlobalNameIdentifier(String name) {
        LuaPsiFile file = createDummyFile(name + "=true; nop=" + name);

        final LuaAssignmentStatement expressionStatement =
                (LuaAssignmentStatement)  file.getLastChild();

        assert expressionStatement != null;

        final LuaReferenceElement ref = (LuaReferenceElement) expressionStatement.getRightExprs().getFirstChild();

        return (LuaIdentifier) ref.getElement();
    }

    @Override
    public LuaIdentifier createFieldNameIdentifier(String name) {
        LuaPsiFile file = createDummyFile("a."+name+"=nil");

        LuaAssignmentStatement assign = (LuaAssignmentStatement) getChildOfFirstStatement(file);

        assert assign != null;
        LuaReferenceElement element = assign.getLeftExprs().getReferenceExprs()[0];
        LuaCompoundIdentifier id = (LuaCompoundIdentifier) element.getElement();

        return (LuaIdentifier) id.getRightSymbol();
    }

    private PsiElement getChildOfFirstStatement(LuaPsiFile file) {
        return file.getFirstChild().getNextSibling();
    }
}
