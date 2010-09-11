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
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiType;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameter;
import org.jetbrains.annotations.NotNull;

import static com.sylvanaar.idea.Lua.lang.psi.LuaPsiType.VOID;


public class LuaParameterImpl extends LuaIdentifierImpl implements LuaPsiElement, LuaParameter {
    public LuaParameterImpl(@NotNull ASTNode node) {
        super(node);
    }

    public String toString() {
        return "Parameter ("+getText()+")";
    }

    @Override
    public LuaFunctionDefinition getDeclaringFunction() {
        return (LuaFunctionDefinition) getNode().getTreeParent().getTreeParent().getPsi(); 

    }

    @Override
    public boolean isVarArgs() {
        return (getNode().getElementType() == LuaElementTypes.ELLIPSIS);        
    }

    @NotNull
    @Override
    public LuaPsiType getType() {
        return VOID;  
    }

//    @Override
//    public PsiElement setName(@NotNull @NonNls String s) throws IncorrectOperationException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }


}
