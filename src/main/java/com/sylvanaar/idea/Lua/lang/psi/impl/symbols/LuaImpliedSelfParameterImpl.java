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

package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 12, 2010
 * Time: 10:52:46 AM
 */
public class LuaImpliedSelfParameterImpl extends LuaParameterImpl
        implements LuaParameter {
    public LuaImpliedSelfParameterImpl(@NotNull ASTNode node) {
        super(node);
    }


    @Override
    public LuaFunctionDefinition getDeclaringFunction() {
        return (LuaFunctionDefinition) getNode().getTreeParent().getPsi(); 

    }

    public String getName() {
        return "self";
    }

    public String getText() { return "self"; }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public LuaExpression getAliasElement() {
        LuaFunctionDefinitionStatement func = (LuaFunctionDefinitionStatement) getParent();
        LuaCompoundIdentifier name = (LuaCompoundIdentifier) func.getIdentifier();

        return (LuaExpression) name.getScopeIdentifier();
    }
}
