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
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpressionList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes.COMMA;


/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 13, 2010
 * Time: 7:18:16 AM
 */
public class LuaExpressionListImpl extends LuaExpressionImpl implements LuaExpressionList {
    public LuaExpressionListImpl(ASTNode node) {
        super(node);
    }

    @Override
    public int count() {
        return getLuaExpressions().size();
    }

    public PsiElement getContext() {
        return getParent();
    }

    public List<LuaExpression> getLuaExpressions() {
        return Arrays.asList(findChildrenByClass(LuaExpression.class));
    }

    public String toString() {
        return "Expression List (Count " + count() + ")";
    }


    @Override
    public PsiElement addAfter(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        List<LuaExpression> exprs = getLuaExpressions();

        if (exprs.size() == 0) {
            add(element);
        } else {
            element = super.addAfter(element, anchor);
            final ASTNode astNode = getNode();
            if (anchor != null) {
                astNode.addLeaf(COMMA, ",", element.getNode());
            } else {
                astNode.addLeaf(COMMA, ",", element.getNextSibling().getNode());
            }
            CodeStyleManager.getInstance(getManager().getProject()).reformat(this);
        }

        return element;
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        List<LuaExpression> exprs = getLuaExpressions();

        if (exprs.size() == 0) {
            add(element);
        } else {
            element = super.addBefore(element, anchor);
            final ASTNode astNode = getNode();
            if (anchor != null) {
                astNode.addLeaf(COMMA, ",", anchor.getNode());
            } else {
                astNode.addLeaf(COMMA, ",", element.getNode());
            }
            CodeStyleManager.getInstance(getManager().getProject()).reformat(this);
        }

        return element;
    }


}
