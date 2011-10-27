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

package com.sylvanaar.idea.Lua.lang.psi.visitor;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocFieldReference;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocPsiElement;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocReferenceElement;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocTag;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiKeywordImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiTokenImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.statements.LuaRepeatStatementImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaCompoundReferenceElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 7:39:03 AM
 */

public class LuaElementVisitor extends PsiElementVisitor {
    public void visitElement(LuaPsiElement element) {
        ProgressManager.checkCanceled();
    }
//
//    public void visitFile(LuaPsiFile e) {
//        visitElement(e);
//    }

    public void visitFunctionDef(LuaFunctionDefinitionStatement e) {
        visitStatement(e);
    }

    public void visitAssignment(LuaAssignmentStatement e) {
        visitStatement(e);
    }

    public void visitIdentifier(LuaIdentifier e) {
        visitElement(e);
    }

    public void visitStatement(LuaStatementElement e) {
        visitElement(e);
    }

    public void visitNumericForStatement(LuaNumericForStatement e) {
        visitStatement(e);
    }

    public void visitBlock(LuaBlock e) {
        visitElement(e);
    }

    public void visitGenericForStatement(LuaGenericForStatement e) {
        visitStatement(e);
    }

    public void visitIfThenStatement(@NotNull LuaIfThenStatement e) {
        visitStatement(e);
    }

    public void visitWhileStatement(LuaWhileStatement e) {
        visitStatement(e);
    }

    public void visitParameter(LuaParameter e) {
        visitElement(e);
    }

    public void visitReturnStatement(LuaReturnStatement e) {
        visitStatement(e);
    }

    public void visitReferenceElement(LuaReferenceElement e) {
        visitElement(e);
    }

    public void visitKeyword(LuaPsiKeywordImpl e) {
        visitElement(e);
    }

    public void visitLuaToken(LuaPsiTokenImpl e) {
        visitElement(e);
    }

    public void visitDeclarationStatement(LuaDeclarationStatement e) {
        visitStatement(e);
    }

    public void visitDeclarationExpression(LuaDeclarationExpression e) {
        visitElement(e);
    }

    public void visitLiteralExpression(LuaLiteralExpression e) {
        visitElement(e);
    }

    public void visitTableConstructor(LuaTableConstructor e) {
        visitElement(e);
    }

    public void visitUnaryExpression(LuaUnaryExpression e) {
        visitElement(e);
    }

    public void visitBinaryExpression(LuaBinaryExpression e) {
        visitElement(e);
    }

    public void visitFunctionCall(LuaFunctionCallExpression e) {
        visitElement(e);
    }

    public void visitBreakStatement(LuaBreakStatement e) {
        visitStatement(e);
    }

    public void visitRepeatStatement(LuaRepeatStatementImpl e) {
        visitStatement(e);
    }

    public void visitFunctionCallStatement(LuaFunctionCallStatement e) {
        visitStatement(e);
    }

    public void visitCompoundIdentifier(LuaCompoundIdentifier e) {
        visitElement(e);
    }

    public void visitCompoundReference(LuaCompoundReferenceElementImpl e) {
        visitElement(e);
    }

    public void visitModuleExpression(LuaModuleExpression e) {
        visitFunctionCall(e);
    }

    public void visitRequireExpression(LuaRequireExpression e) {
        visitFunctionCall(e);
    }

    public void visitDocTag(LuaDocTag e) {
        visitDocComment(e);
    }

    public void visitDocFieldReference(LuaDocFieldReference e) {
        visitDocComment(e);
    }

    public void visitDoStatement(LuaDoStatement e) {
        visitElement(e);
    }

    public void visitDocComment(LuaDocPsiElement e) {
        visitElement(e);
    }

    public void visitAnonymousFunction(LuaAnonymousFunctionExpression e) {
        visitElement(e);
    }

    public void visitDocReference(LuaDocReferenceElement e) {
        visitDocComment(e);
    }
}




