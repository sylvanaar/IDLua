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
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
* Created by IntelliJ IDEA.
* User: Jon S Akhtar
* Date: Jun 14, 2010
* Time: 11:23:33 PM
*/
public class LuaVariableImpl extends LuaReferenceExpressionImpl implements LuaVariable {
    public LuaVariableImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "Variable: " + getText();
    }

    @Override
    public PsiElement resolve() {

        LuaNamedElement e = findChildByClass(LuaDeclarationExpression.class);
        if (e!=null) return e;
        
        LuaGlobalIdentifier g = findChildByClass(LuaGlobalIdentifier.class);
        if (g!=null &&  g.isAssignedTo()) return g;

        return null;
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        LuaNamedElement e = findChildByClass(LuaDeclarationExpression.class);
        if (e != null && !processor.execute(e, state))
            return false;

        LuaGlobalIdentifier g = findChildByClass(LuaGlobalIdentifier.class);
        if (g!=null &&  g.isAssignedTo())
            if (!processor.execute(g, state)) return false;

        return super.processDeclarations(processor, state, lastParent, place);    //To change body of overridden methods use File | Settings | File Templates.
    }

    //    @Override
//    public PsiElement resolve() {
//        LuaNamedElement name = getPrimaryIdentifier();
//        if (name == null)
//            return null;
//
//        return ResolveUtil.treeWalkUp(new ResolveUtil.ResolveProcessor(name.getName()), this, this, this);
//    }
//
//
//    @Nullable
//    private LuaDeclarationExpression getDeclaration() {
//            return findChildByClass(LuaDeclarationStatement.class);
//    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitReferenceExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitReferenceExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return null;
    }

    @Override
    public LuaNamedElement getPrimaryIdentifier() {

        LuaNamedElement e = findChildByClass(LuaDeclarationExpression.class);
        if (e!=null) return e;

        
        return findChildByClass(LuaReferenceExpression.class);
    }
}
