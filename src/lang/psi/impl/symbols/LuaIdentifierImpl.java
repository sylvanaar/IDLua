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
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaVariable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 11, 2010
 * Time: 2:33:37 PM
 */
public abstract class LuaIdentifierImpl extends LuaSymbolImpl implements LuaIdentifier {

    public LuaIdentifierImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
      visitor.visitIdentifier(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitIdentifier(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Nullable
    @NonNls
    public String getName() {
      return getText();
    }

    @Override
    public String toString() {
        return "Identifier: " + getName();
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newExpr, boolean b) {
        return LuaPsiUtils.replaceElement(this, newExpr);
    }

    @Override
    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    /** Defined Value Implementation **/
//    SoftReference<LuaExpression> definedValue = null;
//    @Override
//    public LuaExpression getAssignedValue() {
//        return definedValue == null ? null : definedValue.get();
//    }
//
//    @Override
//    public void setAssignedValue(LuaExpression value) {
//        definedValue = new SoftReference<LuaExpression>(value);
//    }

//    @NotNull
//    @Override
//    public LuaType getLuaType() {
//        if (getAssignedValue() != null)
//            return getAssignedValue().getLuaType();
//
//        return super.getLuaType();
//    }

    /** Defined Value Implementation **/




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

            for (LuaSymbol e : s.getLeftExprs().getSymbols())
                if (e == getParent().getParent())
                    return true;
        }
        else if (parent instanceof LuaFunctionDefinitionStatement) {
            LuaFunctionDefinitionStatement s = (LuaFunctionDefinitionStatement)parent;

            if (s.getIdentifier() == getParent().getParent())
                return true;
        }


        return false;
    }

}
