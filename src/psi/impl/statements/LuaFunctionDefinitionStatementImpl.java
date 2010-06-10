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

package com.sylvanaar.idea.Lua.psi.impl.statements;

import com.intellij.lang.ASTNode;
import com.sylvanaar.idea.Lua.psi.LuaParameterList;
import com.sylvanaar.idea.Lua.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.psi.statements.LuaDoStatement;
import com.sylvanaar.idea.Lua.psi.statements.LuaFunctionDefinitionStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:40:55 AM
 */
public class LuaFunctionDefinitionStatementImpl extends LuaPsiElementImpl implements LuaFunctionDefinitionStatement {

    public LuaFunctionDefinitionStatementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public LuaParameterList getParameters() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
