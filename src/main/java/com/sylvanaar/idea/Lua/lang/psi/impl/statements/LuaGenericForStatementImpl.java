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

package com.sylvanaar.idea.Lua.lang.psi.impl.statements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.sylvanaar.idea.Lua.lang.psi.LuaControlFlowOwner;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.Instruction;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.impl.ControlFlowBuilder;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaGenericForStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 13, 2010
 * Time: 2:12:45 AM
 */
public class LuaGenericForStatementImpl extends LuaStatementElementImpl implements LuaGenericForStatement, LuaControlFlowOwner {
    public LuaGenericForStatementImpl(ASTNode node) {
        super(node);
    }


    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {

        PsiElement parent = place.getParent();
        while(parent != null && !(parent instanceof LuaPsiFile)) {
            if (parent == getBlock() ) {
                LuaExpression[] names = getIndices();
                for (LuaExpression name : names) {
                     if (!processor.execute(name, resolveState)) return false;
                }
            }

            parent = parent.getParent();
        }
       return true;
    }

    @Override
    public LuaSymbol[] getIndices() {
        return findChildrenByClass(LuaLocalDeclaration.class);
    }

    @Override
    public LuaExpression getInClause() {
        return findChildrenByClass(LuaExpressionList.class)[0];
    }

    @Override
    public LuaBlock getBlock() {
        return findChildByClass(LuaBlock.class);
    }

        @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitGenericForStatement(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitGenericForStatement(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public List<? extends LuaLocalDeclaration> getProvidedVariables() {
        return Arrays.asList((LuaLocalDeclaration[]) getIndices());
    }

    @Override
    public Instruction[] getControlFlow() {
        assert isValid();
        CachedValue<Instruction[]> controlFlow = getUserData(CONTROL_FLOW);
        if (controlFlow == null) {
            controlFlow = CachedValuesManager.getManager(getProject()).createCachedValue(new CachedValueProvider<Instruction[]>() {
                @Override
                public Result<Instruction[]> compute() {
                    return Result.create(new ControlFlowBuilder(getProject()).buildControlFlow(LuaGenericForStatementImpl.this), getContainingFile());
                }
            }, false);
            putUserData(CONTROL_FLOW, controlFlow);
        }

        return controlFlow.getValue();
    }
}
