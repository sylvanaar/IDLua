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
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.impl.LuaDocCommentUtil;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaAnonymousFunctionExpression;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaParameterList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaLocalDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaFunction;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import com.sylvanaar.idea.Lua.util.LuaAtomicNotNullLazyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.BLOCK;
import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.PARAMETER_LIST;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 4, 2010
 * Time: 7:44:04 AM
 */
public class LuaAnonymousFunctionExpressionImpl extends LuaExpressionImpl
    implements LuaAnonymousFunctionExpression {
    LuaAtomicNotNullLazyValue<LuaFunction> myLazyType = new LuaAtomicNotNullLazyValue<LuaFunction>() {
            @NotNull
            @Override
            protected LuaFunction compute() {
                LuaFunction type = new LuaFunction();
                acceptLuaChildren(LuaAnonymousFunctionExpressionImpl.this,
                    new LuaPsiUtils.LuaBlockReturnVisitor(type));

                return type;
            }
        };

    public LuaAnonymousFunctionExpressionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitAnonymousFunction(this);
    }

    @Override
    public void accept(@NotNull
    PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitAnonymousFunction(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public LuaParameterList getParameters() {
        return (LuaParameterList) findChildByType(PARAMETER_LIST);
    }

    @Override
    public LuaBlock getBlock() {
        return (LuaBlock) findChildByType(BLOCK);
    }

    @Override
    public TextRange getRangeEnclosingBlock() {
        final PsiElement rparen = findChildByType(LuaElementTypes.RPAREN);

        if (rparen == null) {
            return getTextRange();
        }

        return TextRange.create(rparen.getTextOffset() + 1,
            getTextRange().getEndOffset());
    }

    @Nullable
    @Override
    public LuaParameter getImpliedSelf() {
        return null;
    }

    @Override
    public ItemPresentation getPresentation() {
        return LuaPsiUtils.getFunctionPresentation(this);
    }

    @Override
    public String getPresentableText() {
        LuaSymbol id = getIdentifier();

        return (id != null) ? id.getText() : null;
    }

    @Override
    public String getLocationString() {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Icon getIcon(boolean open) {
        return LuaIcons.LUA_FUNCTION;
    }

    @Override
    public String getPresentationText() {
        return getPresentableText();
    }

    public boolean processDeclarations(@NotNull
    PsiScopeProcessor processor, @NotNull
    ResolveState resolveState, PsiElement lastParent, @NotNull
    PsiElement place) {
        if ((lastParent != null) && (lastParent.getParent() == this)) {
            final LuaParameter[] params = getParameters().getLuaParameters();

            for (LuaParameter param : params) {
                if (!processor.execute(param, resolveState)) {
                    return false;
                }
            }
        }

        return true;
    }

    @NotNull
    @Override
    public LuaFunction getLuaType() {
        return myLazyType.getValue();
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myLazyType.drop();
    }

    @Override
    public String getName() {
        LuaSymbol id = getIdentifier();

        return (id != null) ? id.getName() : null;
    }

    @Override
    public LuaSymbol getIdentifier() {
        LuaExpressionList exprlist = PsiTreeUtil.getParentOfType(this,
                LuaExpressionList.class);

        if (exprlist == null) {
            return null;
        }

        int idx = exprlist.getLuaExpressions().indexOf(this);

        if (idx < 0) {
            return null;
        }

        PsiElement assignment = exprlist.getParent();

        LuaIdentifierList idlist = null;

        if (assignment instanceof LuaAssignmentStatement) {
            idlist = ((LuaAssignmentStatement) assignment).getLeftExprs();
        }

        if (assignment instanceof LuaLocalDefinitionStatement) {
            idlist = ((LuaAssignmentStatement) assignment).getLeftExprs();
        }

        if ((idlist != null) && (idlist.count() > idx)) {
            return idlist.getSymbols()[idx];
        }

        return null;
    }

    @Nullable
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
        return Arrays.asList(getParameters().getLuaParameters());
    }
}
