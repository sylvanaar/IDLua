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
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 13, 2010
 * Time: 8:16:33 AM
 */
public class LuaIdentifierListImpl extends LuaExpressionImpl implements LuaIdentifierList {
    public LuaIdentifierListImpl(ASTNode node) {
        super(node);
    }

    @Override
    public int count() {
        return findChildrenByClass(LuaIdentifier.class).length;
    }

    @Override
    public LuaIdentifier[] getIdentifiers() {
        return findChildrenByClass(LuaIdentifier.class);
    }


    @Override
    public LuaDeclarationExpression[] getDeclarations() {
        return findChildrenByClass(LuaDeclarationExpression.class);
    }

    public String toString() {
        return "Identifier List (Count " + count() + ")";
    }

    @Override
    public LuaReferenceExpression[] getReferenceExprs() {
        return findChildrenByClass(LuaReferenceExpression.class);
    }

//    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
//                                       @NotNull ResolveState resolveState,
//                                       PsiElement lastParent,
//                                       @NotNull PsiElement place) {
//
//       // log.info("decls " + this);
//        final PsiElement[] children = getChildren();
//        for (PsiElement child : children) {
////            if (child == lastParent) break;
////            if (!child.processDeclarations(processor, resolveState, lastParent, place)) return false;
//            if (child instanceof LuaGlobalDeclaration)
//                if (!processor.execute(child, resolveState))
//                    return false;
//
////            if (child instanceof LuaLocalDeclaration)
//        }
//        return true;
//    }

}
