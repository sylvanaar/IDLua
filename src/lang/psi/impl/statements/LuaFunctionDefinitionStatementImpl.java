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
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameterList;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaImpliedSelfParameterImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:40:55 AM
 */
public class LuaFunctionDefinitionStatementImpl extends LuaStatementElementImpl implements LuaFunctionDefinitionStatement/*, PsiModifierList */ {
    private LuaParameterList parameters = null;
    private LuaFunctionIdentifier identifier = null;
    private LuaBlock block = null;
    private boolean definesSelf = false;

    public LuaFunctionDefinitionStatementImpl(ASTNode node) {
        super(node);

        assert getBlock() != null;
    }

    public void accept(LuaElementVisitor visitor) {
        visitor.visitFunctionDef(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitFunctionDef(this);
        } else {
            visitor.visitElement(this);
        }
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

         LuaParameter self = findChildByClass(LuaImpliedSelfParameterImpl.class);

         if (self != null) {
             if (!processor.execute(self, resolveState)) return false;
         }
       }

       if (!getIdentifier().isLocal())
        return true;
        
       return processor.execute(getIdentifier(), resolveState);
    }


    @Nullable
    @NonNls
    public String getName() {
        LuaFunctionIdentifier name = getIdentifier();

        return name != null ? name.getFunctionName() : "anonymous";
    }


    @Override
    public LuaFunctionIdentifier getIdentifier() {
        if (identifier == null) {
            PsiElement e = findChildByType(LuaElementTypes.FUNCTION_IDENTIFIER_SET);
            if (e != null)
                identifier = (LuaFunctionIdentifier) e;
        }
        return identifier;
    }

    @Override
    public LuaParameterList getParameters() {
        if (parameters == null) {
            PsiElement e = findChildByType(LuaElementTypes.PARAMETER_LIST);
            if (e != null)
                parameters = (LuaParameterList) e;
        }
        return parameters;
    }

    public LuaBlock getBlock() {
        if (block == null) {
            PsiElement e = findChildByType(LuaElementTypes.BLOCK);
            if (e != null)
                block = (LuaBlock) e;
        }
        return block;
    }


    @Override
    public String toString() {
        return "Function Declaration (" + getIdentifier() + ")";
    }
}
