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

package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/4/11
 * Time: 10:36 PM
 */
public class LuaWrapperReferenceElementImpl extends LuaReferenceElementImpl implements LuaExpression {
    @Override
    public boolean isSameKind(LuaSymbol symbol) {
//        assert false;
        if (getElement() instanceof LuaSymbol)
             return ((LuaSymbol) getElement()).isSameKind(symbol);
        return false;
    }

    public LuaWrapperReferenceElementImpl(ASTNode node) {
        super(node);
    }

    public PsiElement getElement() {
        return findChildByClass(LuaIdentifier.class);
    }

    public PsiReference getReference() {
        return this;
    }

    @Override
    public String getName() {
        return ((PsiNamedElement)getElement()).getName();
    }

    @Override
    public String toString() {
        return "Reference: " + getName();
    }

}
