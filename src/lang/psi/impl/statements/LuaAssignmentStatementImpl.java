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
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiType;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.LuaVisitor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
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
    List<LuaIdentifier> identifierList = new ArrayList<LuaIdentifier>();

    public LuaAssignmentStatementImpl(ASTNode node) {
        super(node);
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
    public PsiExpression getLExpression() {
        return getLeftExprs();
    }

    @Override
    public PsiExpression getRExpression() {
        return getRightExprs();
    }

    @NotNull
    @Override
    public PsiJavaToken getOperationSign() {
        return null;
    }

    @NotNull
    @Override
    public IElementType getOperationTokenType() {
        return LuaElementTypes.ASSIGN;
    }

    @Override
    public PsiType getType() {
        return null;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaVisitor) {
            ((LuaVisitor) visitor).visitAssignment(this);
        } else {
            visitor.visitElement(this);
        }
    }


}
