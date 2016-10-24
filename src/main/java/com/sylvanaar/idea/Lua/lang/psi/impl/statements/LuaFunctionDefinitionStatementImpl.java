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
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.lang.InferenceCapable;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.impl.LuaDocCommentUtil;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaImpliedSelfParameterImpl;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaParameterList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.InferenceUtil;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaFunction;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:40:55 AM
 */
public class LuaFunctionDefinitionStatementImpl extends LuaStatementElementImpl implements LuaFunctionDefinitionStatement, InferenceCapable/*, PsiModifierList */ {
    final LuaFunction type = new LuaFunction();

    private static final Logger log = Logger.getInstance("Lua.LuaPsiManger");

    public LuaFunctionDefinitionStatementImpl(ASTNode node) {
        super(node);

        assert getBlock() != null;
    }

    @Override
    public void inferTypes() {
        calculateType();
    }

    public LuaType calculateType() {
        type.reset();
        getBlock().acceptChildren(new LuaPsiUtils.LuaBlockReturnVisitor(type));

        LuaSymbol id = getIdentifier();
        type.setNamespace(id.getName());
        if (id instanceof LuaReferenceElement)
            id = (LuaSymbol) ((LuaReferenceElement) id).getElement();

        if (id != null) {
            id.setLuaType(type);
        }

        return type;
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

    @Override
    public ItemPresentation getPresentation() {
        return LuaPsiUtils.getFunctionPresentation(this);
    }

    @Override
    public String getPresentableText() {
        return getPresentationText();
    }

    @Override
    public String getLocationString() {
        return null;
    }


    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        InferenceUtil.requeueIfPossible(this);
    }

    @Override
    public String getPresentationText() {
        return getIdentifier().getText();
    }

    @Override
    public Icon getIcon(boolean open) {
        return LuaIcons.LUA_FUNCTION;
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState resolveState, PsiElement lastParent, @NotNull PsiElement place) {

        LuaSymbol v = getIdentifier();
        if (!processor.execute(v, resolveState)) return false;

        PsiElement parent = place.getParent();

        if (parent != null && PsiTreeUtil.isAncestor(getBlock(), parent, false)) {
            final LuaParameter[] params = getParameters().getLuaParameters();
            for (LuaParameter param : params) {
                if (!processor.execute(param, resolveState)) return false;
            }
            LuaParameter self = findChildByClass(LuaImpliedSelfParameterImpl.class);

            if (self != null) {
                if (!processor.execute(self, resolveState)) return false;
            }
        }

        return true;
    }


    @Nullable
    @NonNls
    public String getName() {
        LuaSymbol name = getIdentifier();

        return name.getName();
    }

    @Override
    public PsiElement setName(String s) {
        return getIdentifier().setName(s);
    }


    @NotNull
    @Override
    public LuaSymbol getIdentifier() {
        LuaReferenceElement e = findChildByClass(LuaReferenceElement.class);
        if (e != null) {
            return (LuaSymbol) e.getElement();
        }

        LuaDeclarationExpression e2 = findChildByClass(LuaDeclarationExpression.class);
        if (e2 != null)
            return e2;

        throw new IllegalStateException("no identifier");
    }

    @Override
    public LuaParameterList getParameters() {
        PsiElement e = findChildByType(LuaElementTypes.PARAMETER_LIST);
        if (e != null) return (LuaParameterList) e;

        return null;
    }

    @Override
    public TextRange getRangeEnclosingBlock() {
        final PsiElement rparen = findChildByType(LuaElementTypes.RPAREN);
        if (rparen == null) return getTextRange();
        return TextRange.create(rparen.getTextRange().getEndOffset(), getTextRange().getEndOffset());
    }

    @Nullable
    @Override
    public LuaParameter getImpliedSelf() {
        return findChildByClass(LuaImpliedSelfParameterImpl.class);
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        throw new IllegalAccessError("cannot replace statement");
    }

    @NotNull
    @Override
    public LuaFunction getLuaType() {
        return type;
    }

    @Override
    public void setLuaType(LuaType type) {
        throw new IllegalAccessError("cannot get type of statment");
    }

    @Override
    public Object evaluate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return LuaDocCommentUtil.findDocComment(this);
    }

    @Override
    public boolean isDeprecated() {
        return false;
    }

    @Override
    public List<? extends LuaLocalDeclaration> getProvidedVariables() {
        final ArrayList<LuaParameter> parameters = new ArrayList<>(Arrays.asList(getParameters().getLuaParameters()));

        final LuaParameter impliedSelf = getImpliedSelf();
        if (impliedSelf != null) {
            parameters.add(0, impliedSelf);
        }
        return parameters;
    }
}
