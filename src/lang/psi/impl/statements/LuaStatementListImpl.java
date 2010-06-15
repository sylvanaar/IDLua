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

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementList;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 10:17:49 PM
 */
public class LuaStatementListImpl extends LuaPsiElementImpl implements PsiCodeBlock, LuaStatementList {
    Deque<LuaStatementElement> statements = new LinkedList<LuaStatementElement>();
    
    public LuaStatementListImpl(ASTNode node) {
        super(node);
        LuaStatementElement[] stats =findChildrenByClass(LuaStatementElement.class);
        statements.addAll(Arrays.asList(stats));
    }
    
    public void acceptChildren(LuaElementVisitor visitor) {
      PsiElement child = getFirstChild();
      while (child != null) {
        if (child instanceof LuaStatementElement) {
          ((LuaPsiElement) child).accept(visitor);
        }

        child = child.getNextSibling();
      }
    }
    
    @NotNull
    @Override
    public PsiStatement[] getStatements() {
        return statements.toArray(new PsiStatement[statements.size()]);
    }

    @Override
    public PsiElement getFirstBodyElement() {
        return statements.getFirst();
    }

    @Override
    public PsiElement getLastBodyElement() {
        return statements.getLast();
    }

    @Override
    public PsiJavaToken getLBrace() {
        return null;
    }

    @Override
    public PsiJavaToken getRBrace() {
        return null;
    }
}
