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

import com.intellij.lang.*;
import com.intellij.openapi.diagnostic.*;
import com.intellij.psi.*;
import com.intellij.psi.scope.*;
import com.intellij.psi.tree.*;
import com.intellij.psi.util.*;
import com.sylvanaar.idea.Lua.lang.parser.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.lists.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.types.InferenceUtil;
import com.sylvanaar.idea.Lua.lang.psi.util.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import com.sylvanaar.idea.Lua.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:40:55 AM
 */
public class LuaAssignmentStatementImpl extends LuaStatementElementImpl implements LuaAssignmentStatement {
    private static final Logger log = Logger.getInstance("Lua.AssignmentStmt");
    public LuaAssignmentStatementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitAssignment(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitAssignment(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public LuaIdentifierList getLeftExprs() {
        return (LuaIdentifierList) findChildByType(LuaElementTypes.IDENTIFIER_LIST);
    }

    @Override
    public LuaExpressionList getRightExprs() {
        return (LuaExpressionList) findChildByType(LuaElementTypes.EXPR_LIST);
    }

    @Override
    public String toString() {
        return super.toString() + " " + getText().substring(0, Math.min(getTextLength(), 20));
    }

    LuaAssignmentUtil.Assignments assignments = new LuaAssignmentUtil.Assignments(this);

    @NotNull
    @Override
    public LuaAssignment[] getAssignments() {
        return assignments.getValue();
    }


    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        log.debug("Subtree Changed: " + toString());
        assignments.drop();
        definedAndAssignedSymbols.drop();
        InferenceUtil.requeueIfPossible(this);
    }

    @Override
    public LuaExpression getAssignedValue(LuaSymbol symbol) {
        return LuaAssignment.FindAssignmentForSymbol(getAssignments(), symbol);
    }

    @NotNull
    @Override
    public IElementType getOperationTokenType() {
        return LuaElementTypes.ASSIGN;
    }


    @Override
    public PsiElement getOperatorElement() {
        return findChildByType(getOperationTokenType());
    }

    @Override
    public void inferTypes() {
//        log.debug("transfer types: "+toString());
        LuaAssignmentUtil.transferTypes(this);
//        InferenceUtil.inferAssignment(this);
    }


    DefAndAssignSymbols definedAndAssignedSymbols = new DefAndAssignSymbols();

    @Override
    public LuaSymbol[] getDefinedAndAssignedSymbols() {
        return definedAndAssignedSymbols.getValue();
    }

    @Override
    public LuaExpression[] getDefinedSymbolValues() {
        return LuaAssignmentUtil.getDefinedSymbolValues(this);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {

        if (PsiTreeUtil.isAncestor(this, place, true)) return true;

        LuaSymbol[] defs = getDefinedAndAssignedSymbols();
        for (LuaSymbol def : defs) {
            if (def instanceof LuaReferenceElement)
                def = (LuaSymbol) ((LuaReferenceElement) def).getElement();

            if (def instanceof Assignable && !(def instanceof LuaLocalIdentifier))
                if (!processor.execute(def, state)) return false;
        }

        return true; //LuaPsiUtils.processChildDeclarations(, processor, state, lastParent, place);
    }

    @Override
    public LuaSymbol[] getDefinedSymbols() {
        List<LuaSymbol> names = new ArrayList<LuaSymbol>();

        LuaIdentifierList leftExprs = getLeftExprs();
        if (leftExprs == null)
            return LuaSymbol.EMPTY_ARRAY;

        LuaSymbol[] lhs = leftExprs.getSymbols();
        for (LuaSymbol symbol : lhs) {
            if (symbol instanceof Assignable)
                names.add(symbol);
        }

        return names.toArray(new LuaSymbol[names.size()]);
    }

    private class DefAndAssignSymbols extends LuaAtomicNotNullLazyValue<LuaSymbol[]> {
        @NotNull
        @Override
        protected LuaSymbol[] compute() {
            LuaAssignment[] assignments = getAssignments();

            if (assignments.length == 0) return LuaSymbol.EMPTY_ARRAY;

            List<LuaSymbol> syms = new ArrayList<LuaSymbol>();
            for (int i = 0, assignmentsLength = assignments.length; i < assignmentsLength; i++) {
                LuaAssignment assign = assignments[i];

                LuaSymbol id = assign.getSymbol();
                if (id instanceof Assignable || (id instanceof LuaReferenceElement &&
                                                               ((LuaReferenceElement) id)
                                                                       .getElement() instanceof Assignable))
                    syms.add(id);
            }
            if (syms.size() == 0) return LuaSymbol.EMPTY_ARRAY;

            return syms.toArray(new LuaSymbol[syms.size()]);
        }
    }

}
