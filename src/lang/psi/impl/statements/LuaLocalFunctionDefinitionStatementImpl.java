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
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.util.*;
import org.jetbrains.annotations.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 10/22/10
 * Time: 1:41 AM
 */
public class LuaLocalFunctionDefinitionStatementImpl extends LuaFunctionDefinitionStatementImpl implements LuaFunctionDefinitionStatement  {
    public LuaLocalFunctionDefinitionStatementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getIdentifier().getName();
    }

    @NotNull
    public LuaSymbol getIdentifier() {
        return findChildByClass(LuaSymbol.class);
    }

    final LuaFunction type = new LuaFunction();

    public LuaType calculateType() {
        type.reset();
        getBlock().accept(new LuaPsiUtils.LuaBlockReturnVisitor(type));
        getIdentifier().setLuaType(type);
        return type;
    }

    public Assignable getDeclaration() {
        return (Assignable) getIdentifier();
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState resolveState,
                                       PsiElement lastParent, @NotNull PsiElement place) {

        LuaSymbol v = getIdentifier();
        if (v != null) if (!processor.execute(v, resolveState)) return false;

        PsiElement parent = place.getParent();
        while (parent != null && !(parent instanceof LuaPsiFile)) {
            if (parent == getBlock()) {
                final LuaParameter[] params = getParameters().getLuaParameters();
                for (LuaParameter param : params) {
                    if (!processor.execute(param, resolveState)) return false;
                }

                break;
            }

            parent = parent.getParent();
        }

        return super.processDeclarations(processor, resolveState, lastParent, place);
    }
}
