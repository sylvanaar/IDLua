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
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaGlobalIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/15/11
 * Time: 1:31 AM
 */
public class LuaGlobalDeclarationImpl extends LuaGlobalIdentifierImpl implements LuaGlobalIdentifier, LuaDeclarationExpression {
    public LuaGlobalDeclarationImpl(ASTNode node) {
        super(node);
    }
    public boolean  isDeclaration() {
        return true;
    }


    @Override
    public String toString() {
        return "Global Declaration: " + getText();
    }

    @Override
    public LuaIdentifier getNameSymbol() {
        return this;  
    }

    @Override
    public String getDefinedName() {
        return getName();
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
}
