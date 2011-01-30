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
package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiType;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;
public class LuaParameterImpl extends LuaIdentifierImpl implements LuaParameter {
    public LuaParameterImpl(@NotNull
    ASTNode node) {
        super(node);
    }

    public String toString() {
        return "Parameter: " + getText();
    }

    @Override
    public LuaFunctionDefinition getDeclaringFunction() {
        return (LuaFunctionDefinition) getNode().getTreeParent().getTreeParent()
                                           .getPsi();
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitParameter(this);
    }

    @Override
    public void accept(@NotNull
    PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitParameter(this);
        } else {
            visitor.visitElement(this);
        }
    }



    @Override
    public boolean isVarArgs() {
        return (getNode().getElementType() == LuaElementTypes.ELLIPSIS);
    }

    @NotNull
    @Override
    public PsiType getType() {
        return PsiType.VOID;
    }

    @Override
    public LuaIdentifier getNameSymbol() {
        return findChildByClass(LuaIdentifier.class);
    }

    @Override
    public String getDefinedName() {
        return getName();
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement setName(@NotNull
    String s) {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        return processor.execute(this,  state);
    }


    @Override
    public boolean isSameKind(LuaSymbol identifier) {
        return identifier instanceof LuaUpvalueIdentifier || identifier instanceof LuaLocalIdentifier;
    }
}
