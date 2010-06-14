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
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiStatement;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementList;
import org.jetbrains.annotations.NotNull;

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
        ASTNode[] stats = getNode().getChildren(LuaElementTypes.STATEMENT_SET);
        for(int i=0; i<stats.length-1; i++)
            statements.add((LuaStatementElement)stats[i].getPsi());
    }


    @NotNull
    @Override
    public PsiStatement[] getStatements() {
        return (LuaStatementElement[])statements.toArray();
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
