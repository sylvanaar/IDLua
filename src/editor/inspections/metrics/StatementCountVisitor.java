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
package com.sylvanaar.idea.Lua.editor.inspections.metrics;

import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;


class StatementCountVisitor extends LuaElementVisitor {
  private int statementCount = 0;

//  public void visitElement(LuaPsiElement element) {
//    int oldCount = 0;
//    if (element instanceof LuaFunctionDefinitionStatement) {
//      oldCount = statementCount;
//    }
//    super.visitElement(element);
//
//    if (element instanceof LuaFunctionDefinitionStatement) {
//      statementCount = oldCount;
//    }
//  }


    public void visitFunctionDef(LuaFunctionDefinitionStatement e) {
        super.visitFunctionDef(e);

        statementCount = 0;
    }


  public void visitStatement(@NotNull LuaStatementElement statement) {
    statementCount++;
    int oldCount = 0;

    if (statement instanceof LuaFunctionDefinitionStatement) {
      oldCount = statementCount;
    }

    super.visitStatement(statement);

    if (statement instanceof LuaFunctionDefinitionStatement) {
      statementCount = oldCount;
    }
  }


  public int getStatementCount() {
    return statementCount;
  }
}