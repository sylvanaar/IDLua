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

import com.intellij.lang.*;
import com.intellij.navigation.*;
import com.intellij.openapi.util.*;
import com.intellij.psi.*;
import com.intellij.psi.scope.*;
import com.intellij.psi.util.*;
import com.sylvanaar.idea.Lua.*;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.impl.LuaDocCommentUtil;
import com.sylvanaar.idea.Lua.lang.parser.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.lists.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.util.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import com.sylvanaar.idea.Lua.util.LuaAtomicNotNullLazyValue;
import org.jetbrains.annotations.*;

import javax.swing.*;

import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 4, 2010
 * Time: 7:44:04 AM
 */
public class LuaAnonymousFunctionExpressionImpl extends LuaExpressionImpl implements LuaAnonymousFunctionExpression {
    public LuaAnonymousFunctionExpressionImpl(ASTNode node) {
        super(node);


    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitAnonymousFunction(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
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
        if (rparen == null) return getTextRange();
        return TextRange.create(rparen.getTextOffset()+1, getTextRange().getEndOffset());
    }

    @Override
    public ItemPresentation getPresentation() {
        return LuaPsiUtils.getFunctionPresentation(this);
    }

    @Override
    public String getPresentableText() {
        LuaSymbol id = getIdentifier();

        return id != null ? id.getText() : null;
    }

    @Override
    public String getLocationString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Icon getIcon(boolean open) {
        return LuaIcons.LUA_FUNCTION;
    }
    @Override
    public String getPresentationText() {
        return getPresentableText();
    }
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {

       if (lastParent != null && lastParent.getParent() == this) {
         final LuaParameter[] params = getParameters().getLuaParameters();
         for (LuaParameter param : params) {
           if (!processor.execute(param, resolveState)) return false;
         }
       }

        return true;
    }

    LuaAtomicNotNullLazyValue<LuaFunction> myLazyType    = new LuaAtomicNotNullLazyValue<LuaFunction>() {
        @NotNull
        @Override
        protected LuaFunction compute() {
            LuaFunction type = new LuaFunction();
            acceptLuaChildren(LuaAnonymousFunctionExpressionImpl.this, new LuaPsiUtils.LuaBlockReturnVisitor(type));
            return type;
        }
    };

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
        return id != null ? id.getName() : null;
    }

    @Override
    public LuaSymbol getIdentifier() {
        LuaExpressionList exprlist = PsiTreeUtil.getParentOfType(this, LuaExpressionList.class);
        if (exprlist == null)
            return null;

        int idx = exprlist.getLuaExpressions().indexOf(this);
        if (idx < 0)
            return null;

        PsiElement assignment = exprlist.getParent();

        LuaIdentifierList idlist = null;
        if (assignment instanceof LuaAssignmentStatement)
            idlist = ((LuaAssignmentStatement) assignment).getLeftExprs();

        if (assignment instanceof LuaLocalDefinitionStatement)
            idlist = ((LuaLocalDefinitionStatement) assignment).getLeftExprs();

        if (idlist != null && idlist.count() > idx)
            return idlist.getSymbols()[idx];

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
}
