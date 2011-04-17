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
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaLocalDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 6, 2010
 * Time: 10:00:19 AM
 */
public class LuaLocalDefinitionStatementImpl extends LuaStatementElementImpl implements LuaLocalDefinitionStatement, LuaStatementElement, LuaAssignmentStatement {
    public LuaLocalDefinitionStatementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitDeclarationStatement(this);
        //visitor.visitAssignment(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitDeclarationStatement(this);
            //((LuaElementVisitor) visitor).visitAssignment(this);
        } else {
            visitor.visitElement(this);
        }
    }


    @Override
    public LuaDeclarationExpression[] getDeclarations() {
        return findChildByClass(LuaIdentifierList.class).getDeclarations();
    }

    @Override
    public LuaReferenceElement[] getReferenceExprs() {
        return findChildByClass(LuaIdentifierList.class).getReferenceExprs();
    }

    @Override
    public LuaExpression[] getExprs() {
        LuaExpressionList list = findChildByClass(LuaExpressionList.class);
        if (list == null)
            return new LuaExpression[0];

        return list.getLuaExpressions().toArray(new LuaExpression[list.count()]);
    }

    @Override
    public LuaStatementElement replaceWithStatement(LuaStatementElement newCall) {
        return null;
    }

    // locals are undefined within the statement, so  local a,b = b,a
    // should not resolve a to a or b to b. So to handle this we process
    // our declarations unless we are walking from a child of ourself.
    // in our case its, (localstat) <- (expr list) <- (expression) <- (variable) <- (reference )

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {
        // If we weren't found as a parent of the reference
        if (!PsiTreeUtil.isAncestor(this, place, true)) {
            final LuaDeclarationExpression[] decls = getDeclarations();
            for (int i = decls.length-1; i >= 0 ; i--) {
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

    @Override
    public IElementType getOperationTokenType() {
        return LuaTokenTypes.ASSIGN;
    }

    @Override
    public PsiElement getOperatorElement() {
        return findChildByType(getOperationTokenType());
    }

    @Override
    public LuaIdentifierList getDefinedNames() {
        return findChildByClass(LuaIdentifierList.class);
    }

    @Override
    public LuaSymbol[] getDefinedAndAssignedSymbols() {
        LuaExpressionList exprs = getRightExprs();

        if (exprs == null)
            return LuaSymbol.EMPTY_ARRAY;

        List<LuaExpression> vals = exprs.getLuaExpressions();

        if (vals.size() == 0)
            return LuaSymbol.EMPTY_ARRAY;

        LuaSymbol[] syms = new LuaSymbol[vals.size()];

        LuaSymbol[] defs = getLeftExprs().getSymbols();

        for(int i=0;i<syms.length;i++)
            syms[i]=defs[i];

        return syms;
    }

    @Override
    public LuaExpression[] getDefinedSymbolValues() {
        LuaExpressionList exprs = getRightExprs();

        if (exprs == null)
            return LuaExpression.EMPTY_ARRAY;

        List<LuaExpression> vals = exprs.getLuaExpressions();

        return vals.toArray(new LuaExpression[vals.size()]);
    }
}
