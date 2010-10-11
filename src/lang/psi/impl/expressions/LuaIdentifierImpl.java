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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 11, 2010
 * Time: 2:33:37 PM
 */
public class LuaIdentifierImpl extends LuaExpressionImpl implements LuaIdentifier {
    boolean global = false;
    boolean local = false;
    boolean field = false;
    
    public LuaIdentifierImpl(ASTNode node) {
        super(node);

        global = node.getElementType() == LuaElementTypes.GLOBAL_NAME;
        local = node.getElementType() == LuaElementTypes.LOCAL_NAME;
        field = node.getElementType() == LuaElementTypes.FIELD_NAME;
    }
    @Override
    public boolean isDeclaration() {
        return false;
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
        return "Identifier: " + getText();
    }

    @Override
    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        return null;
    }


    @Override
    public boolean isGlobal() {
        return global;
    }

    @Override
    public boolean isLocal() {
        return local;
    }

   @Override
    public boolean isField() {
        return field;
    }

//    @Override
//    public IElementType getTokenType() {
//        return getNode().getElementType();
//    }

//    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
//                                       @NotNull ResolveState resolveState,
//                                       PsiElement lastParent,
//                                       @NotNull PsiElement place) {
//        processor.execute(this, resolveState);
//        return true;
//    }
}
