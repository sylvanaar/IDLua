/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.util;

import com.intellij.openapi.util.NotNullLazyValue;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.statements.LuaAssignmentStatementImpl;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaAlias;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 6/8/11
 * Time: 12:49 PM
 */
public class LuaAssignmentUtil {
    @NotNull
    public static LuaAssignment[] getAssignments(LuaAssignmentStatement assignmentStatement) {
        LuaExpressionList exprs = assignmentStatement.getRightExprs();

        if (exprs == null)
            return LuaAssignment.EMPTY_ARRAY;

        List<LuaExpression> vals = exprs.getLuaExpressions();

        if (vals.size() == 0)
            return LuaAssignment.EMPTY_ARRAY;

        LuaIdentifierList leftExprs = assignmentStatement.getLeftExprs();
        if (leftExprs == null)
            return LuaAssignment.EMPTY_ARRAY;

        LuaSymbol[] defs = leftExprs.getSymbols();

        LuaAssignment[] assignments = new LuaAssignment[Math.min(vals.size(), defs.length)];

        for(int i=0;i<assignments.length; i++)
            assignments[i]= new LuaAssignment(defs[i], vals.get(i));

        return assignments;
    }

    public static LuaExpression[] getDefinedSymbolValues(LuaAssignmentStatementImpl assignmentStmt) {
        LuaExpressionList exprs = assignmentStmt.getRightExprs();

        if (exprs == null) return LuaExpression.EMPTY_ARRAY;

        List<LuaExpression> vals = exprs.getLuaExpressions();

        return vals.toArray(new LuaExpression[vals.size()]);
    }

    public static void transferTypes(LuaAssignmentStatement statement) {
        for (LuaAssignment a : statement.getAssignments()) {
            final LuaSymbol symbol = a.getSymbol();
            final LuaType lType = symbol.getLuaType();
            final LuaType rType = a.getValue().getLuaType();
            transferSingleType(a.getValue(), symbol, lType, rType);
        }
    }

    public static void transferSingleType(LuaExpression value, LuaSymbol symbol, LuaType lType, LuaType rType) {
        if (symbol instanceof LuaDeclarationExpression)
            symbol.setLuaType(rType);
        else
            symbol.setLuaType(LuaType.combineTypes(lType, rType));

        if (symbol instanceof LuaAlias)
            ((LuaAlias) symbol).setAliasElement(value);
    }

    public static class Assignments extends NotNullLazyValue<LuaAssignment[]> {
        private LuaAssignmentStatement assignment;

        public Assignments(LuaAssignmentStatement assignment) {this.assignment = assignment;}

        @NotNull
        @Override
        protected LuaAssignment[] compute() {
            return getAssignments(assignment);
        }
    }
}
