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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaGetTableExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaVariable;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaGetTableExpressionImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
* Created by IntelliJ IDEA.
* User: Jon S Akhtar
* Date: Jun 14, 2010
* Time: 11:23:33 PM
*/
public class LuaVariableImpl extends LuaReferenceElementImpl implements LuaVariable {
    public LuaVariableImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "Variable: " + getText();
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitCompoundReferenceExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitCompoundReferenceExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public PsiElement getPrimaryIdentifier() {
        PsiElement e = getFirstChild();

        if (e instanceof LuaIdentifier)
            return e;


        while (e instanceof LuaGetTableExpression)
            e = ((LuaGetTableExpressionImpl)e).getLeftSymbol();
        
        return e;
    }

    @Override
    public LuaIdentifier reduceToIdentifier() {
        PsiElement e = getFirstChild();

        if (e instanceof LuaIdentifier)
            return (LuaIdentifier) e;

        return null;
    }

    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        PsiElement id = getPrimaryIdentifier();
        return id!=null?id.getResolveScope():GlobalSearchScope.EMPTY_SCOPE;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
         PsiElement id = getPrimaryIdentifier();
         return id!=null?id.getUseScope():super.getUseScope();
    }

    @Override
    public boolean isSameKind(LuaSymbol symbol) {
        LuaIdentifier i = reduceToIdentifier();

        if (i != null)
            return i.isSameKind(symbol);

        return symbol instanceof LuaVariable || symbol instanceof LuaFieldIdentifier;
    }

    @Override
    public boolean isAssignedTo() {
        return getParent() instanceof LuaFunctionDefinitionStatement ||
                (getParent().getParent() instanceof LuaAssignmentStatement && getParent() instanceof LuaIdentifierList);
    }

    @Override
    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {
        if (isAssignedTo())
            if (!processor.execute(this, resolveState))
                return false;

        return super.processDeclarations(processor, resolveState, lastParent, place);
    }


     public TextRange getRangeInElement() {
        final PsiElement id = getPrimaryIdentifier();
        final ASTNode nameElement = id!=null?id.getNode():null;
        final int startOffset = nameElement != null ? nameElement.getStartOffset() : getNode().getTextRange().getEndOffset();
        return new TextRange(startOffset - getNode().getStartOffset(), getTextLength());
    }

}
