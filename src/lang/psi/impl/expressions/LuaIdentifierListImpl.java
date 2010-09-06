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

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDeclaration;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 13, 2010
 * Time: 8:16:33 AM
 */
public class LuaIdentifierListImpl extends LuaExpressionImpl implements LuaIdentifierList {
    public LuaIdentifierListImpl(ASTNode node) {
        super(node);
    }

    @Override
    public int count() {
        return findChildrenByClass(LuaIdentifier.class).length;
    }

    @Override
    public LuaDeclaration[] getDeclarations() {
        return findChildrenByClass(LuaDeclaration.class);
    }


    @Override
    public boolean isDeclaration() {
        return getContext() instanceof LuaAssignmentStatement;
    }

    @Override
    public LuaReferenceExpression[] getReferenceExprs() {
        return findChildrenByClass(LuaReferenceExpression.class);
    }



}
