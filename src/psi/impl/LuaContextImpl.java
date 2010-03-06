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

package com.sylvanaar.idea.Lua.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.annotator.LuaElementVisitor;
import com.sylvanaar.idea.Lua.psi.LuaContext;
import com.sylvanaar.idea.Lua.psi.LuaDirective;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 14.07.2009
 * Time: 01:02:29
 */
public class LuaContextImpl extends LuaElementImpl implements LuaContext {

    public LuaContextImpl(ASTNode node) {
        super(node);
    }

    public LuaDirective getDirective() {
        return (LuaDirective) getNode().getTreeParent().getPsi();
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitContext(this);
        } else {
            visitor.visitElement(this);
        }
    }

}