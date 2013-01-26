/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElementFactory;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaLiteralExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaStubElementBase;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaStringLiteralExpressionImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaStubUtils;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaCompoundIdentifierStub;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaAssignment;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import com.sylvanaar.idea.Lua.util.LuaAtomicNullableLazyValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/20/11
 * Time: 3:44 AM
 */
public class LuaCompoundIdentifierImpl extends LuaStubElementBase<LuaCompoundIdentifierStub>
        implements LuaCompoundIdentifier {

    public LuaCompoundIdentifierImpl(ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement getParent() {
         return SharedImplUtil.getParent(getNode());
    }

    public LuaCompoundIdentifierImpl(LuaCompoundIdentifierStub stub) {
        this(stub, LuaElementTypes.GETTABLE);
    }
    public LuaCompoundIdentifierImpl(LuaCompoundIdentifierStub stub, IStubElementType type) {
        super(stub, type);
        myType = LuaStubUtils.GetStubOrPrimitiveType(stub);
    }

//    /** Defined Value Implementation **/
//    SoftReference<LuaExpression> definedValue = null;
//    @Override
//    public LuaExpression getAssignedValue() {
//        return definedValue == null ? null : definedValue.get();
//    }
//
//    @Override
//    public void setAssignedValue(LuaExpression value) {
//        definedValue = new SoftReference<LuaExpression>(value);
//        setLuaType(value.getLuaType());
//    }
//    /** Defined Value Implementation **/

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitCompoundIdentifier(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitCompoundIdentifier(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Nullable
    public LuaExpression getRightSymbol() {
        LuaExpression[] e = findChildrenByClass(LuaExpression.class);
        return e.length>1?e[1]:null;
    }

    @Nullable
    public LuaExpression getLeftSymbol() {
        LuaExpression[] e = findChildrenByClass(LuaExpression.class);
        return e.length>0?e[0]:null;
    }

//    @Override
//    public int getStartOffsetInParent() {
//        return getLeftSymbol().getTextLength();
//    }

    private String asString(LuaExpression e) {
//        Object eval = e.evaluate();
//        if (eval == null) return "{"+e.getText()+"}";
//
//        return eval.toString();

        return e.getText();
    }
    
    @Nullable
    @Override
    public String toString() {
        try {
        return "GetTable: " +  asString(getLeftSymbol()) + getOperator() + asString(getRightSymbol()) + (getOperator() == "[" ? "]" : "");
        } catch (Throwable t) { return "err"; }
    }

    @Override
    public String getOperator() {
        try {
        return findChildByType(LuaElementTypes.TABLE_ACCESS).getText();
        } catch (Throwable t) { return "err"; }
    }

    @Override
    public LuaCompoundIdentifier getEnclosingIdentifier() {
        LuaPsiElement e = (LuaPsiElement) getParentByStub();
        if (e instanceof LuaCompoundIdentifier)
            return (LuaCompoundIdentifier) e;

        final PsiElement parent = getParent();

        final PsiReference reference = parent instanceof PsiReference ? (PsiReference) parent : null;

        if (reference == null) return null;

        if (parent.getParent() instanceof LuaCompoundIdentifier)
            return (LuaCompoundIdentifier) parent.getParent();

        return this;
    }

    @Override
    public boolean isCompoundDeclaration() {
        final LuaCompoundIdentifierStub stub = getStub();
        if (stub != null)
            return stub.isGlobalDeclaration();

//        PsiElement e = getParent().getParent();
//        return e instanceof LuaIdentifierList || e instanceof LuaFunctionDefinition;

        return isAssignedTo();
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        return true;
    }


    //    @Override
//    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
//                                       @NotNull ResolveState state, PsiElement lastParent,
//                                       @NotNull PsiElement place) {
//        //LuaPsiUtils.processChildDeclarations(this, processor, state, lastParent, place);
//        if (isCompoundDeclaration()) {
//            if (!processor.execute(this,state)) return false;
//            final LuaExpression rightSymbol = getRightSymbol();
//            if (rightSymbol!=null) {
//                if (!processor.execute(rightSymbol, state)) {
//                    return false;
//                }
//            }
//        }
//
//        return true;
//    }

    @Override
    public PsiElement getScopeIdentifier() {
        PsiElement child = getFirstChild();

        if (child instanceof LuaCompoundReferenceElementImpl)
            child = ((LuaCompoundReferenceElementImpl) child).getElement();

        if (child instanceof LuaCompoundIdentifier)
            return ((LuaCompoundIdentifier) child).getScopeIdentifier();

        if (child instanceof LuaReferenceElement)
            return ((LuaReferenceElement) child).getElement();

        return null;
    }

    @Override
    public boolean isSameKind(LuaSymbol symbol) {
        return symbol instanceof LuaFieldIdentifier || symbol instanceof LuaGlobalDeclarationImpl;
    }



    @Override
    public PsiReference getReference() {
        if (getStub() != null)
            return null;
        return (PsiReference) getParent();
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return PsiReference.EMPTY_ARRAY;
    }

    //    @Override
//    public int getStartOffsetInParent() {
//        final LuaExpression rightSymbol = getRightSymbol();
//        return getTextOffset()- (rightSymbol != null ? rightSymbol.getTextOffset() : 0);
//    }

    @Override
    public boolean isAssignedTo() {
        // This should return true if this variable is being assigned to in the current statement
        // it will be used for example by the global identifier class to decide if it should resolve
        // as a declaration or not

        PsiElement parent = getParent();
        while (!(parent instanceof LuaStatementElement)) {
            parent = parent.getParent();
        }

        if (parent instanceof LuaAssignmentStatement) {
            LuaAssignmentStatement s = (LuaAssignmentStatement)parent;

            for (LuaAssignment assignment : s.getAssignments())
                if (assignment.getSymbol() == this)
                    return true;
        }
        else if (parent instanceof LuaFunctionDefinitionStatement) {
            LuaFunctionDefinitionStatement s = (LuaFunctionDefinitionStatement)parent;

            if (s.getIdentifier() == this)
                return true;
        }


        return false;
    }

    public boolean isIdentifier(final String name, final Project project) {
        return LuaPsiElementFactory.getInstance(project).createReferenceNameFromText(name) != null;
    }


    NameLazyValue name = new NameLazyValue();

    @Override
    public String getDefinedName() {
        final LuaCompoundIdentifierStub stub = getStub();
        if (stub != null) {
            return stub.getName();
        }

        return name.getValue();
    }

    @Override
    @Nullable
    public String getName() {
        final LuaCompoundIdentifierStub stub = getStub();
        if (stub != null) {
            return stub.getName();
        }

        return name.getValue();
    }

    LuaType myType = LuaPrimitiveType.ANY;

    @Override
    public void setLuaType(LuaType type) {
        myType = type;

        LuaExpression l = getLeftSymbol();
        if (l == null)
            return;
        LuaType t = l.getLuaType();

        LuaExpression r = getRightSymbol();

        if (r == null)
            return;

        Object field = null;
        if (r instanceof LuaFieldIdentifier)
            field = r.getText();
        else if (r instanceof LuaLiteralExpression)
            field = ((LuaLiteralExpression) r).getValue();

        if (t instanceof LuaTable && field != null) {
            ((LuaTable) t).addPossibleElement(field, type);

            r.setLuaType(type);
        }
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newExpr, boolean removeUnnecessaryParentheses) {
        return LuaPsiUtils.replaceElement(this, newExpr);
    }


    @NotNull @Override
    public LuaType getLuaType() {
        if (myType instanceof StubType)
            myType = ((StubType) myType).get();

        if (myType instanceof LuaTypeSet)
            if (((LuaTypeSet) myType).getTypeSet().size() == 1)
                myType = ((LuaTypeSet) myType).getTypeSet().iterator().next();


        return myType;
    }


    @Override
    public Object evaluate() {
        return null;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        return LuaPsiUtils.replaceElement(this, LuaPsiElementFactory.getInstance(getProject()).createIdentifier(name));
    }

    public PsiElement getNameIdentifier() {
        return this;
    }

    private class NameLazyValue extends LuaAtomicNullableLazyValue<String> {
        @Nullable @Override
        protected String compute() {
            ApplicationManager.getApplication().assertReadAccessAllowed();

            LuaExpression rhs = getRightSymbol();
            if (rhs == null)
                return null;
            if (rhs instanceof LuaStringLiteralExpressionImpl) {
                String s = (String) ((LuaStringLiteralExpressionImpl) rhs).getValue();
                if (getOperator().equals("[") && isIdentifier(s, getProject())) {

                    final LuaExpression lhs = getLeftSymbol();
                    if (lhs instanceof LuaNamedElement) {
                        return lhs.getName() + "." + s;
                    }
                }
            }

            LuaExpression lhs = getLeftSymbol();

            String text = getText();
            if (!(lhs instanceof LuaSymbol))
                return null;

            int leftLen = lhs.getTextLength();
            return lhs.getName() + text.substring(leftLen);
        }
    }
}
