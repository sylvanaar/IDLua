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
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.psi.LuaIdentifier;
import com.sylvanaar.idea.Lua.psi.LuaParameterList;
import com.sylvanaar.idea.Lua.psi.impl.LuaIdentifierImpl;
import com.sylvanaar.idea.Lua.psi.impl.LuaParameterListImpl;
import com.sylvanaar.idea.Lua.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.psi.statements.LuaFunctionBase;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 11, 2010
 * Time: 3:53:29 AM
 */
public abstract class LuaFunctionBaseImpl extends LuaPsiElementImpl implements LuaFunctionBase {
    private LuaParameterList parameters = null;
    private LuaIdentifier identifier = null;

    public LuaFunctionBaseImpl(ASTNode node) {
        super(node);
    }

    @Override
    public LuaIdentifier getIdentifier() {
        if (identifier  == null) {
        PsiElement e = findChildByType(LuaElementTypes.FUNCTION_IDENTIFIER);
        if (e != null)
            identifier = (LuaIdentifierImpl) e;
        }
        return identifier;
    }

    @Override
    public LuaParameterList getParameters() {
        if (parameters  == null) {
        PsiElement e = findChildByType(LuaElementTypes.PARAMETER_LIST);
        if (e != null)
            parameters = (LuaParameterListImpl) e;
        }
        return parameters;
    }
}
