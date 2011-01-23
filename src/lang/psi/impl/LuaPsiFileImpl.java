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

package com.sylvanaar.idea.Lua.lang.psi.impl;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDeclarationStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 12:19:03 PM
 */
public class LuaPsiFileImpl extends LuaPsiFileBaseImpl implements LuaPsiFile {
    public LuaPsiFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, LuaFileType.LUA_LANGUAGE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return LuaFileType.LUA_FILE_TYPE;
    }


   @Override
   public String toString() {
    return "Lua script";
  }

    @Override
    public GlobalSearchScope getFileResolveScope() {
        return new EverythingGlobalScope();
    }



    @Override
    public LuaStatementElement addStatementBefore(@NotNull LuaStatementElement statement, LuaStatementElement anchor) throws IncorrectOperationException {
        return null;
    }

    @Override
    public void removeElements(PsiElement[] elements) throws IncorrectOperationException {

    }

    @Override
    public void removeVariable(LuaIdentifier variable) {

    }

    @Override
    public LuaDeclarationStatement addVariableDeclarationBefore(LuaDeclarationStatement declaration, LuaStatementElement anchor) throws IncorrectOperationException {
        return null;
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {
        final PsiElement[] children = getChildren();
        for (PsiElement child : children) {
            if (child == lastParent) break;
            if (!child.processDeclarations(processor, resolveState, lastParent, place)) return false;
        }
        return true;
    }


  public void accept(LuaElementVisitor visitor) {
    visitor.visitFile(this);
  }

  public void acceptChildren(LuaElementVisitor visitor) {
    PsiElement child = getFirstChild();
    while (child != null) {
      if (child instanceof LuaPsiElement) {
        ((LuaPsiElement) child).accept(visitor);
      }

      child = child.getNextSibling();
    }
  }
}
