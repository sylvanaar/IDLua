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
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 10/22/10
 * Time: 1:41 AM
 */
public class LuaLocalFunctionDefinitionStatementImpl extends LuaFunctionDefinitionStatementImpl
{
    public LuaLocalFunctionDefinitionStatementImpl(ASTNode node) {
        super(node);
    }



        public LuaSymbol getIdentifier() {
            return findChildByClass(LuaSymbol.class);
        }

        public LuaDeclarationExpression getDeclaration() {
            return (LuaDeclarationExpression) getIdentifier();
        }

        public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                   @NotNull ResolveState resolveState,
                                   PsiElement lastParent,
                                   @NotNull PsiElement place) {

        LuaSymbol v = getIdentifier();
        if (v != null)
           if (!processor.execute(v, resolveState))
                return false;

            
        PsiElement parent = place.getParent();
        while (parent != null && !(parent instanceof LuaPsiFile)) {
            if (parent == getBlock()) {
                final LuaParameter[] params = getParameters().getParameters();
                for (LuaParameter param : params) {
                    if (!processor.execute(param, resolveState)) return false;
                }
            }

            parent = parent.getParent();
        }



            
        return true;
    }
}
