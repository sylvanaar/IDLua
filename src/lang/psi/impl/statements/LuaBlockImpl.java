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
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 10:17:49 PM
 */
public class LuaBlockImpl extends LuaPsiElementImpl implements LuaBlock {
    public LuaBlockImpl(ASTNode node) {
        super(node);
    }
    
    public void accept(LuaElementVisitor visitor) {
        visitor.visitBlock(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {

            ((LuaElementVisitor) visitor).visitBlock(this);
        } else {
            visitor.visitElement(this);
        }
    }

    public LuaStatementElement[] getLuaStatements() {
        return findChildrenByClass(LuaStatementElement.class);
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {

        PsiElement parent = place.getParent();
        while (parent != null && !(parent instanceof LuaPsiFile)) {
            if (parent == this) {
                if (!processor.execute(this, resolveState)) return false;
            }

            parent = parent.getParent();
        }

        return true;
    }

//    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
//        if (lastParent != null && lastParent.getParent() == this) {
//
//        }
////        return ResolveUtil.processChildren(this, processor, state, lastParent, place);
//    }

    @NotNull
    @Override
    public PsiStatement[] getStatements() {
        return getLuaStatements();
    }

    @Override
    public PsiElement getFirstBodyElement() {
        return null; 
    }

    @Override
    public PsiElement getLastBodyElement() {
        return null;
    }

    @Override
    public PsiJavaToken getLBrace() {
        return null;
    }

    @Override
    public PsiJavaToken getRBrace() {
        return null;
    }

    @Override
    public boolean shouldChangeModificationCount(PsiElement place) {
        return true;
    }
}
