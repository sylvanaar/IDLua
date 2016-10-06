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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaModuleExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementFactoryImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaStubElementBase;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaStubUtils;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaGlobalDeclarationStub;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaAlias;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaPrimitiveType;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaTable;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import com.sylvanaar.idea.Lua.lang.psi.types.StubType;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import com.sylvanaar.idea.Lua.lang.psi.util.SymbolUtil;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/15/11
 * Time: 1:31 AM
 */
public class LuaGlobalDeclarationImpl extends LuaStubElementBase<LuaGlobalDeclarationStub>
        implements LuaGlobalDeclaration {
    public LuaGlobalDeclarationImpl(ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement getParent() {
        return getDefinitionParent();
    }

    public LuaGlobalDeclarationImpl(LuaGlobalDeclarationStub stub) {
        super(stub, LuaElementTypes.GLOBAL_NAME_DECL);
        type = LuaStubUtils.GetStubOrPrimitiveType(stub);
    }

    @Override
    public String toString() {
        return "Global Decl: " + getGlobalEnvironmentName();
    }

    @Override
    public String getDefinedName() {
        return getGlobalEnvironmentName();
    }


    @Override @Nullable
    public String getModuleName() {
        final LuaGlobalDeclarationStub stub = getStub();
        if (stub != null) {
            return stub.getModule();
        }
        if (!isValid()) return null;
        
        LuaPsiFile file = (LuaPsiFile) getContainingFile();
        if (file == null)
            return null;
        
        return file.getModuleNameAtOffset(getTextOffset());
    }


    @Override
    public String getGlobalEnvironmentName() {
        return SymbolUtil.getGlobalEnvironmentName(this);
    }


    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState state, PsiElement lastParent,
                                       @NotNull PsiElement place) {
        return !(processor.execute(this, state));
    }


    @Override
    public String getName() {
        final LuaGlobalDeclarationStub stub = getStub();
        if (stub != null) {
            return stub.getName();
        }

        return super.getText();  
    }

    @Override
    public Object evaluate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitDeclarationExpression(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitDeclarationExpression(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newExpr, boolean removeUnnecessaryParentheses) {
        return LuaPsiUtils.replaceElement(this, newExpr);
    }

    @Override
    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        LuaDeclarationExpression decl = LuaPsiElementFactoryImpl.getInstance(getProject()).createGlobalNameIdentifierDecl(name);

        return replace(decl);
    }

    @NotNull
    @Override
    public IStubElementType getElementType() {
        return LuaElementTypes.GLOBAL_NAME_DECL;
    }

    @Override
    public boolean isSameKind(LuaSymbol symbol) {
        return symbol instanceof LuaGlobalIdentifier || symbol instanceof LuaCompoundIdentifierImpl;
    }

    @Override
    public boolean isAssignedTo() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private LuaType type = LuaPrimitiveType.ANY;

    @NotNull @Override
    public LuaType getLuaType() {
        if (type instanceof StubType) {
            type = ((StubType) type).get();
            LuaModuleExpression module = SymbolUtil.getModule(this);
            if (module != null)
                ((LuaTable) module.getLuaType()).addPossibleElement(getName(), type);
        }

        return type;
    }

    @Override
    public void setLuaType(LuaType type) {
        this.type = type;

//        if (getStub() != null) {
        LuaModuleExpression module = SymbolUtil.getModule(this);
        if (module != null)
            ((LuaTable) module.getLuaType()).addPossibleElement(getName(), this.type);
//        }
    }


    @Override
    public PsiReference getReference() {
        if (getParent() instanceof PsiReference && ((PsiReference) getParent()).getElement().equals(this))
            return (PsiReference) getParent();

        return super.getReference();
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        if (this == another)
            return true;

        if (another instanceof LuaReferenceElement)
            another = ((LuaReferenceElement) another).getElement();

        if (another instanceof LuaAlias) {
            final PsiElement aliasElement = ((LuaAlias) another).getAliasElement();
            if (aliasElement instanceof LuaSymbol)
                if (isEquivalentTo(aliasElement))
                    return true;
        }

        if (another instanceof LuaSymbol) {
            String myName = getName();
            if (myName == null)
                return false;
            return myName.equals(((LuaSymbol) another).getName()) && ((LuaSymbol) another).isSameKind(this);
        }

        return false;
    }
}
