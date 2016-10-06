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
import com.intellij.psi.*;
import com.intellij.psi.scope.*;
import com.intellij.psi.tree.*;
import com.intellij.psi.util.*;
import com.sylvanaar.idea.Lua.lang.lexer.*;
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
 * Date: Sep 6, 2010
 * Time: 10:00:19 AM
 */
public class LuaLocalDefinitionStatementImpl extends LuaStatementElementImpl implements LuaLocalDefinitionStatement,
        LuaStatementElement, LuaAssignmentStatement {
    public LuaLocalDefinitionStatementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitDeclarationStatement(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitDeclarationStatement(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " " + getText().substring(0, Math.min(getTextLength(), 20));
    }


    LuaAtomicNotNullLazyValue<LuaDeclarationExpression[]> declarations = new Declarations();

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        declarations.drop();
        assignments.drop();
        InferenceUtil.requeueIfPossible(this);
    }

    @Override
    public LuaDeclarationExpression[] getDeclarations() {
        return declarations.getValue();
    }

    @Override
    public LuaExpression[] getExprs() {
        LuaExpressionList list = findChildByClass(LuaExpressionList.class);
        if (list == null) return new LuaExpression[0];

        return list.getLuaExpressions().toArray(new LuaExpression[list.count()]);
    }



    // locals are undefined within the statement, so  local a,b = b,a
    // should not resolve a to a or b to b. So to handle this we process
    // our declarations unless we are walking from a child of ourself.
    // in our case its, (localstat) <- (expr list) <- (expression) <- (variable) <- (reference )

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState resolveState,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        // If we weren't found as a parent of the reference
        if (!PsiTreeUtil.isAncestor(this, place, true)) {
            final LuaDeclarationExpression[] decls = getDeclarations();
            for (int i = decls.length - 1; i >= 0; i--) {
                LuaDeclarationExpression decl = decls[i];
                if (!processor.execute(decl, resolveState)) return false;
            }
        }

        return true;
    }

    @Override
    public LuaIdentifierList getLeftExprs() {
        return findChildByClass(LuaIdentifierList.class);
    }

    @Override
    public LuaExpressionList getRightExprs() {
        return findChildByClass(LuaExpressionList.class);
    }

    LuaAssignmentUtil.Assignments assignments = new LuaAssignmentUtil.Assignments(this);

    @NotNull
    @Override
    public LuaAssignment[] getAssignments() {
        return assignments.getValue();
    }


    @Override
    public LuaExpression getAssignedValue(LuaSymbol symbol) {
        return LuaAssignment.FindAssignmentForSymbol(getAssignments(), symbol);
    }


    @Override
    public IElementType getOperationTokenType() {
        return LuaTokenTypes.ASSIGN;
    }

    @Override
    public PsiElement getOperatorElement() {
        return findChildByType(getOperationTokenType());
    }

    @Override
    public void inferTypes() {
        LuaAssignmentUtil.transferTypes(this);
    }

    @Override
    public LuaSymbol[] getDefinedSymbols() {
        return getDeclarations();
    }

    @Override
    public LuaLocalDeclaration[] getDefinedAndAssignedSymbols() {
        LuaAssignment[] assignments = getAssignments();

        if (assignments.length == 0) return LuaLocalDeclaration.EMPTY_ARRAY;

        LuaLocalDeclaration[] syms = new LuaLocalDeclaration[assignments.length];
        for (int i = 0, assignmentsLength = assignments.length; i < assignmentsLength; i++) {
            LuaAssignment assign = assignments[i];
            syms[i] = (LuaLocalDeclaration) assign.getSymbol();
        }
        return syms;
    }

    @Override
    public LuaExpression[] getDefinedSymbolValues() {
        LuaExpressionList exprs = getRightExprs();

        if (exprs == null) return LuaExpression.EMPTY_ARRAY;

        List<LuaExpression> vals = exprs.getLuaExpressions();

        return vals.toArray(new LuaExpression[vals.size()]);
    }

    private class Declarations extends LuaAtomicNotNullLazyValue<LuaDeclarationExpression[]> {
        @NotNull
        @Override
        protected LuaDeclarationExpression[] compute() {
            List<LuaDeclarationExpression> decls = new ArrayList<LuaDeclarationExpression>();
            LuaIdentifierList list = findChildByClass(LuaIdentifierList.class);

            assert list != null;
            for(LuaSymbol sym : list.getSymbols()) {
                if (sym instanceof LuaDeclarationExpression)
                    decls.add((LuaDeclarationExpression) sym);
                else if (sym instanceof LuaReferenceElement) {
                    PsiElement e = ((LuaReferenceElement) sym).getElement();

                    if (e instanceof Assignable)
                        decls.add((LuaDeclarationExpression) e);
                }
            }

            return decls.toArray(new LuaDeclarationExpression[decls.size()]);
        }
    }


}
