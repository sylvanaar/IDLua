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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/4/11
 * Time: 10:36 PM
 */
public class LuaWrapperReferenceElementImpl extends LuaReferenceElementImpl {
    @Override
    public boolean isSameKind(LuaSymbol symbol) {
        assert false;
        return false;  //To change body of implemented methods use File | Settings | File Templates.
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

    public TextRange getRangeInElement() {
        final ASTNode nameElement = getNameElement();
        final int startOffset = nameElement != null ? nameElement.getStartOffset() : getNode().getTextRange().getEndOffset();
        return new TextRange(startOffset - getNode().getStartOffset(), getTextLength());
    }

    public ASTNode getNameElement() {
        PsiElement e = findChildByClass(LuaIdentifier.class);

        if (e != null)
            return e.getNode();

        return null;
    }

    @Override
    public String toString() {
        return "Reference: " + getName();
    }
}
