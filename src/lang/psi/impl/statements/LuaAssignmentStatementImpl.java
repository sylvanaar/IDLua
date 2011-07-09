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
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaAssignment;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaAssignmentUtil;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:40:55 AM
 */
public class LuaAssignmentStatementImpl extends LuaStatementElementImpl implements LuaAssignmentStatement {
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

    @NotNull
    @Override
    public LuaAssignment[] getAssignments() {
        return LuaAssignmentUtil.getAssignments(this);
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
    public LuaSymbol[] getDefinedAndAssignedSymbols() {
        LuaAssignment[] assignments = getAssignments();

        if (assignments.length == 0) return LuaSymbol.EMPTY_ARRAY;

        List<LuaSymbol> syms = new ArrayList<LuaSymbol>();
        for (int i = 0, assignmentsLength = assignments.length; i < assignmentsLength; i++) {
            LuaAssignment assign = assignments[i];

            LuaSymbol id = assign.getSymbol();
            if (id instanceof LuaDeclarationExpression || (id instanceof LuaReferenceElement &&
                                                           ((LuaReferenceElement) id)
                                                                   .getElement() instanceof LuaDeclarationExpression))
                syms.add(assign.getSymbol());
        }
        if (syms.size() == 0) return LuaSymbol.EMPTY_ARRAY;
        
        return syms.toArray(new LuaSymbol[syms.size()]);
    }

    @Override
    public LuaExpression[] getDefinedSymbolValues() {
        return LuaAssignmentUtil.getDefinedSymbolValues(this);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        LuaSymbol[] defs = getDefinedAndAssignedSymbols();
        for (LuaSymbol def : defs) {
            if (def instanceof LuaReferenceElement)
                def = (LuaSymbol) ((LuaReferenceElement) def).getElement();
            
            if (def instanceof LuaDeclarationExpression)
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
            if (symbol instanceof LuaDeclarationExpression)
                names.add(symbol);
        }

        return names.toArray(new LuaSymbol[names.size()]);
    }
}
