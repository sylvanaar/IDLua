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

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.source.PsiFileWithStubSupport;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFileBase;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.Instruction;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.impl.ControlFlowBuilder;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDeclarationStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaRecursiveElementVisitor;
import com.sylvanaar.idea.Lua.util.LuaModuleUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 12:19:03 PM
 */
public class LuaPsiFileImpl extends LuaPsiFileBaseImpl implements LuaPsiFile, PsiFileWithStubSupport, PsiFileEx, LuaPsiFileBase {
    private boolean sdkFile;

    public LuaPsiFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, LuaFileType.LUA_LANGUAGE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return LuaFileType.LUA_FILE_TYPE;
    }


    @Override
    public String toString() {
        return "Lua script: " + getName();
    }

    @Override
    public GlobalSearchScope getFileResolveScope() {
        return new EverythingGlobalScope();
    }


    @Override
    public LuaStatementElement addStatementBefore(@NotNull LuaStatementElement statement, LuaStatementElement anchor) throws IncorrectOperationException {
        return null;
    }

    @Override
    public void removeElements(PsiElement[] elements) throws IncorrectOperationException {

    }

    String moduleName;

    public String getModuleName() {
//        final LuaFileStub stub = (LuaFileStub) getStub();
//        if (stub != null) {
//            return stub.getModule();
//        }
//
//        LuaElementVisitor v = new LuaRecursiveElementVisitor() {
//            public void visitFunctionCallStatement(LuaFunctionCallStatement e) {
//                super.visitFunctionCallStatement(e);
//                try {
//                    if (e instanceof LuaModuleStatement)
//                        moduleName = ((LuaModuleStatement) e).getName();
//
//                } catch (Throwable ignored) {
//                }
//            }
//        };
//
//        accept(v);
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public boolean isSdkFile() {
        LuaModuleUtil.checkForSdkFile(this, getProject());
        return sdkFile;
    }

    @Override
    public void setSdkFile(boolean b) {
        sdkFile = b;
    }


    @Override
    public void removeVariable(LuaIdentifier variable) {

    }

    @Override
    public LuaDeclarationStatement addVariableDeclarationBefore(LuaDeclarationStatement declaration, LuaStatementElement anchor) throws IncorrectOperationException {
        return null;
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {
        final PsiElement[] children = getChildren();
        for (PsiElement child : children) {
            if (child == lastParent) break;
            if (!child.processDeclarations(processor, resolveState, lastParent, place)) return false;
        }
        return true;
    }


    public void accept(LuaElementVisitor visitor) {
        visitor.visitFile(this);
    }

    public void acceptChildren(LuaElementVisitor visitor) {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof LuaPsiElement) {
                ((LuaPsiElement) child).accept(visitor);
            }

            child = child.getNextSibling();
        }
    }


    @Override
    public LuaDeclarationExpression[] getSymbolDefs() {
        final Set<LuaDeclarationExpression> decls =
                new HashSet<LuaDeclarationExpression>();

        LuaElementVisitor v = new LuaRecursiveElementVisitor() {
            public void visitDeclarationExpression(LuaDeclarationExpression e) {
                super.visitDeclarationExpression(e);
                if (!(e instanceof LuaLocalIdentifier))
                    decls.add(e);
            }

            @Override
            public void visitCompoundIdentifier(LuaCompoundIdentifier e) {
                super.visitCompoundIdentifier(e);

                if (e.isAssignedTo())
                    decls.add(e);
            }
        };

        v.visitElement(this);

        return decls.toArray(new LuaDeclarationExpression[decls.size()]);
    }


    @Override
    public LuaStatementElement[] getStatements() {
        final List<LuaStatementElement> stats =
                new ArrayList<LuaStatementElement>();

        LuaElementVisitor v = new LuaRecursiveElementVisitor() {
            public void visitElement(LuaPsiElement e) {
                super.visitElement(e);
                if (e instanceof LuaStatementElement)
                    stats.add((LuaStatementElement) e);
            }
        };

        v.visitElement(this);

        return stats.toArray(new LuaStatementElement[stats.size()]);
    }

    @Override
    public LuaFunctionDefinitionStatement[] getFunctionDefs() {
        final List<LuaFunctionDefinitionStatement> funcs =
                new ArrayList<LuaFunctionDefinitionStatement>();

        LuaElementVisitor v = new LuaRecursiveElementVisitor() {
            public void visitFunctionDef(LuaFunctionDefinitionStatement e) {
                super.visitFunctionDef(e);
                funcs.add(e);
            }
        };

        v.visitElement(this);

        return funcs.toArray(new LuaFunctionDefinitionStatement[funcs.size()]);
    }

    @Override
    public Instruction[] getControlFlow() {
        assert isValid();
        CachedValue<Instruction[]> controlFlow = getUserData(CONTROL_FLOW);
        if (controlFlow == null) {
            controlFlow = CachedValuesManager.getManager(getProject()).createCachedValue(new CachedValueProvider<Instruction[]>() {
                        @Override
                        public Result<Instruction[]> compute() {
                            return Result.create(new ControlFlowBuilder(getProject()).buildControlFlow(LuaPsiFileImpl.this), getContainingFile());
                        }
                    }, false);
            putUserData(CONTROL_FLOW, controlFlow);
        }

        return controlFlow.getValue();
    }
}
