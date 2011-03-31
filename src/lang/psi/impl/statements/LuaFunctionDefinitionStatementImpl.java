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
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameterList;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaCompoundIdentifierImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaImpliedSelfParameterImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
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


    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState resolveState, PsiElement lastParent, @NotNull PsiElement place) {

        LuaSymbol v = getIdentifier();
        if (v != null && (v instanceof LuaGlobalDeclaration || (v instanceof LuaCompoundIdentifierImpl && ((LuaCompoundIdentifierImpl) v).isCompoundDeclaration())))
            if (!processor.execute(v, resolveState)) return false;

        PsiElement parent = place.getParent();
        while (parent != null && !(parent instanceof LuaPsiFile)) {
            if (parent == getBlock()) {
                final LuaParameter[] params = getParameters().getLuaParameters();
                for (LuaParameter param : params) {
                    if (!processor.execute(param, resolveState)) return false;
                }
                LuaParameter self = findChildByClass(LuaImpliedSelfParameterImpl.class);

                if (self != null) {
                    if (!processor.execute(self, resolveState)) return false;
                }

            }

            parent = parent.getParent();
        }


//
//        if (!getBlock().processDeclarations(processor, resolveState, lastParent, place))
//            return false;

//        if (getIdentifier() == null || !getIdentifier().isLocal())
//            return true;


        return true;
    }


    @Nullable
    @NonNls
    public String getName() {
        LuaSymbol name = getIdentifier();

        return name != null ? name.getText() : "anonymous";
    }

    @Override
    public PsiElement setName(String s) {
        return null;//getIdentifier().setName(s);
    }


    @Override
    public LuaSymbol getIdentifier() {
        LuaReferenceElement e = findChildByClass(LuaReferenceElement.class);
        if (e != null) {
            return (LuaSymbol) e.getElement();
        }
        return null;
    }

    @Override
    public String getDocString() {
        return null;
    }

    @Override
    public String getParameterString() {
        return getParameters().getText();
    }

    @Override
    public LuaParameterList getParameters() {
        PsiElement e = findChildByType(LuaElementTypes.PARAMETER_LIST);
        if (e != null) return (LuaParameterList) e;

        return null;
    }

    public LuaBlock getBlock() {
        PsiElement e = findChildByType(LuaElementTypes.BLOCK);
        if (e != null) return (LuaBlock) e;
        return null;
    }


    @Override
    public String toString() {
        return "Function Declaration (" + (getIdentifier() != null ? getIdentifier().getName() : "null") + ")";
    }


    @Override
    public LuaDocComment getDocComment() {
        PsiElement e = getPrevSibling();

        while (e != null && !(e instanceof LuaPsiElement))
            e = e.getPrevSibling();
        
        if (e instanceof LuaDocComment)
            return (LuaDocComment) e;

        return null;
    }

    @Override
    public boolean isDeprecated() {
        return false;
    }
}
