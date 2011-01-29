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
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaAnonymousFunctionExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameterList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import org.jetbrains.annotations.NotNull;

import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.BLOCK;
import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.PARAMETER_LIST;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 4, 2010
 * Time: 7:44:04 AM
 */
public class LuaAnonymousFunctionExpressionImpl extends LuaExpressionImpl implements LuaAnonymousFunctionExpression {
    public LuaAnonymousFunctionExpressionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public LuaParameterList getParameters() {
        return (LuaParameterList) findChildByType(PARAMETER_LIST);
    }

    @Override
    public LuaBlock getBlock() {
        return (LuaBlock) findChildByType(BLOCK);
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {

       if (lastParent != null && lastParent.getParent() == this) {
         final LuaParameter[] params = getParameters().getParameters();
         for (LuaParameter param : params) {
           if (!processor.execute(param, resolveState)) return false;
         }
       }

        return true;
    }
}
