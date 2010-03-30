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

package com.sylvanaar.idea.Lua.psi.statements;


import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.psi.formatter.LuaControlStatement;
import com.sylvanaar.idea.Lua.psi.statements.clauses.LuaForClause;

/**
 * @autor: ilyas
 */
public interface LuaForStatement extends LuaControlStatement,LuaLoopStatement {

  public LuaForClause getClause();

// TODO  public LuaStatement getBody();

  public PsiElement getRParenth();

  public PsiElement getLParenth();

}
