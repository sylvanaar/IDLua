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

import com.intellij.psi.FileResolveScopeProvider;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.psi.statements.LuaRequireStatement;
import com.sylvanaar.idea.Lua.psi.statements.typedef.members.LuaMembersDeclaration;
import com.sylvanaar.idea.Lua.psi.toplevel.packaging.LuaModuleDefinition;
import org.jetbrains.annotations.NotNull;




/**
 * @author ven
 */
public interface LuaFile extends LuaFileBase, FileResolveScopeProvider {

 LuaRequireStatement[] getImportStatements();

  @NotNull
  String getPackageName();

 LuaModuleDefinition getPackageDefinition();

  void setModuleName(String moduleName);

  <T extends LuaMembersDeclaration> T addMemberDeclaration(@NotNull T decl, PsiElement anchorBefore) throws IncorrectOperationException;

  void removeMemberDeclaration (LuaMembersDeclaration decl);
}
