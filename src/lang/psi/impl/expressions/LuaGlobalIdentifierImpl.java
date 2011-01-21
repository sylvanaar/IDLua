/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaGlobalIdentifier;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/15/11
 * Time: 1:31 AM
 */
public class LuaGlobalIdentifierImpl extends LuaIdentifierImpl implements LuaGlobalIdentifier {
    public LuaGlobalIdentifierImpl(ASTNode node) {
        super(node);
    }


    @NotNull
    public SearchScope getUseScope() {
        return new EverythingGlobalScope();
    }

    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        return new EverythingGlobalScope();
    }

    @Override
    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        return null; 
    }


    public boolean  isDeclarationEquals() {
        return isAssignedTo();
    }


    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState state, PsiElement lastParent,
                                       @NotNull PsiElement place) {
        if (isDeclarationEquals()) {
            System.out.println(getName() + " is assigned to");
            if (!processor.execute(this,state)) return false;
        }

        return true;
    }
}
