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

package com.sylvanaar.idea.Lua.lang.psi.visitor;

import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlockStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaIfThenStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 11/18/11
 * Time: 9:07 AM
 */
public class LuaBlockVisitor extends LuaElementVisitor {
    @Override
    public void visitStatement(LuaStatementElement e) {
        super.visitStatement(e);    
        
        if (e instanceof LuaBlockStatement)
            ((LuaBlockStatement) e).getBlock().accept(this);
        
        if (e instanceof LuaIfThenStatement)
            for (LuaBlock block : ((LuaIfThenStatement) e).getAllClauseBlocks()) {
                block.accept(this);
            }
    }

    @Override
    public void visitBlock(LuaBlock e) {
        super.visitBlock(e);

        LuaStatementElement[] statements = e.getStatements();
        for (LuaStatementElement statement : statements) {
            statement.accept(this);
        }
    }
}
