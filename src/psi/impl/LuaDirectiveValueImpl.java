/*
 * Copyright 2009 Max Ishchenko
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

package com.sylvanaar.idea.Lua.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.sylvanaar.idea.Lua.annotator.LuaElementVisitor;
import com.sylvanaar.idea.Lua.psi.LuaDirective;
import com.sylvanaar.idea.Lua.psi.LuaDirectiveValue;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 09.07.2009
 * Time: 21:02:29
 */
public class LuaDirectiveValueImpl extends LuaElementImpl implements LuaDirectiveValue {

    public LuaDirectiveValueImpl(ASTNode node) {
        super(node);
    }

    public LuaDirective getDirective() {
        return ((LuaComplexValueImpl) getNode().getTreeParent().getPsi()).getDirective();
    }


    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitDirectiveValue(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return new FileReferenceSet(this).getAllReferences();
    }

}