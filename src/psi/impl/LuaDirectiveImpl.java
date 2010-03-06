/*
 * Copyright 2009 Max Ishchenko
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

package com.sylvanaar.idea.Lua.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.LuaKeywordsManager;
import com.sylvanaar.idea.Lua.annotator.LuaElementVisitor;
import com.sylvanaar.idea.Lua.lexer.LuaElementTypes;
import com.sylvanaar.idea.Lua.psi.LuaComplexValue;
import com.sylvanaar.idea.Lua.psi.LuaContext;
import com.sylvanaar.idea.Lua.psi.LuaDirective;
import com.sylvanaar.idea.Lua.psi.LuaDirectiveName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 09.07.2009
 * Time: 21:02:29
 */
public class LuaDirectiveImpl extends LuaElementImpl implements LuaDirective {

    private static final TokenSet DIRECTIVE_VALUE_TOKENS = TokenSet.create(LuaElementTypes.COMPLEX_VALUE);

    public LuaDirectiveImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitDirective(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @NotNull
    public String getNameString() {
        return getDirectiveName().getText();
    }

    @NotNull
    public LuaDirectiveName getDirectiveName() {
        ASTNode nameNode = getNode().findChildByType(LuaElementTypes.CONTEXT_NAME);
        if (nameNode == null) {
            nameNode = getNode().findChildByType(LuaElementTypes.DIRECTIVE_NAME);
        }
        //is npe really probable here?
        return (LuaDirectiveName) nameNode.getPsi();
    }

    @Nullable
    public LuaContext getDirectiveContext() {
        ASTNode contextNode = getNode().findChildByType(LuaElementTypes.CONTEXT);
        return contextNode != null ? (LuaContext) contextNode.getPsi() : null;
    }

    @Nullable
    public LuaContext getParentContext() {
        ASTNode parentNode = getNode().getTreeParent();
        if (parentNode.getPsi() instanceof LuaContext) {
            return (LuaContext) parentNode.getPsi();
        } else {
            return null;
        }
    }

    @NotNull
    public List<LuaComplexValue> getValues() {
        ArrayList<LuaComplexValue> result = new ArrayList<LuaComplexValue>();
        for (ASTNode value : getNode().getChildren(DIRECTIVE_VALUE_TOKENS)) {
            result.add((LuaComplexValue) value.getPsi());
        }
        return result;
    }

    public boolean isInChaosContext() {
        return getParentContext() != null && LuaKeywordsManager.CHAOS_DIRECTIVES.contains(getParentContext().getDirective().getNameString());
    }

    public boolean hasContext() {
        return getDirectiveContext() != null;
    }
}

