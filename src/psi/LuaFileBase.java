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

package com.sylvanaar.idea.Lua.psi;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportHolder;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.psi.statements.LuaTopLevelDefintion;
import com.sylvanaar.idea.Lua.psi.statements.LuaVariableDeclaration;
import com.sylvanaar.idea.Lua.psi.statements.typedef.members.LuaMethod;
import com.sylvanaar.idea.Lua.psi.toplevel.LuaTopStatement;
import com.sylvanaar.idea.Lua.psi.toplevel.imports.LuaImportStatement;
import com.sylvanaar.idea.Lua.psi.util.LuaDeclarationHolder;
import com.sylvanaar.idea.Lua.psi.util.LuaStatementOwner;
import com.sylvanaar.idea.Lua.psi.util.LuaVariableDeclarationOwner;
import org.jetbrains.annotations.Nullable;









/**
 * @author ilyas
 */
public interface LuaFileBase extends PsiFile, LuaVariableDeclarationOwner, LuaStatementOwner, PsiClassOwner,LuaControlFlowOwner, PsiImportHolder,
        LuaDeclarationHolder {
  String SCRIPT_BASE_CLASS_NAME = "groovy.lang.Script";
  String[] IMPLICITLY_IMPORTED_PACKAGES = {
      "java.lang",
      "java.util",
      "java.io",
      "java.net",
      "groovy.lang",
      "groovy.util",
  };
  String[] IMPLICITLY_IMPORTED_CLASSES = {
      "java.math.BigInteger",
      "java.math.BigDecimal",
  };

 LuaTopLevelDefintion[] getTopLevelDefinitions();

 LuaMethod[] getTopLevelMethods();

 LuaVariableDeclaration[] getTopLevelVariableDeclarations();

 LuaTopStatement[] getTopStatements();

 LuaImportStatement addImportForClass(PsiClass aClass) throws IncorrectOperationException;

  void removeImport(LuaImportStatement importStatement) throws IncorrectOperationException;

 LuaImportStatement addImport(LuaImportStatement statement) throws IncorrectOperationException;

  boolean isScript();

  @Nullable
  PsiClass getScriptClass();
}
