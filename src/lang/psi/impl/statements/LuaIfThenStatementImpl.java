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

import com.intellij.lang.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.lang.parser.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:40:55 AM
 */
public class LuaIfThenStatementImpl extends LuaStatementElementImpl implements LuaIfThenStatement {

    public LuaIfThenStatementImpl(ASTNode node) {
        super(node);
    }

    public void accept(LuaElementVisitor visitor) {
      visitor.visitIfThenStatement(this);
    }
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitIfThenStatement(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public LuaExpression getIfCondition() {
        return findChildByClass(LuaConditionalExpression.class);
    }

    @Override
    public LuaExpression[] getElseIfConditions() {
        return ArrayUtil.remove(findChildrenByClass(LuaConditionalExpression.class), 0);
    }

    @Override
    public LuaBlock getIfBlock() {
        return findChildrenByClass(LuaBlock.class)[0];
    }

    @Override
    public LuaBlock[] getElseIfBlocks() {
        LuaBlock[] b = getAllClauseBlocks();
        return Arrays.copyOfRange(b, 1, getElseBlock()==null? b.length : b.length-1);
    }

    @Override
    public LuaBlock getElseBlock() {
        final PsiElement elseKeyword = findChildByType(LuaElementTypes.ELSE);
        if (elseKeyword != null) {
            return PsiTreeUtil.getNextSiblingOfType(elseKeyword, LuaBlock.class);
        }
        return null;
    }

    @Override
    public LuaBlock[] getAllClauseBlocks() {
        return findChildrenByClass(LuaBlock.class);
    }
}
