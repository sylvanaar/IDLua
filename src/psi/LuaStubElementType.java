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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.sylvanaar.idea.Lua.LuaFileType;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


/**
 * @author ilyas
 */
public abstract class LuaStubElementType<S extends StubElement, T extends LuaElement> extends IStubElementType<S, T> {

  public LuaStubElementType(@NonNls @NotNull String debugName) {
    super(debugName, LuaFileType.LUA_LANGUAGE);
  }

  public abstract PsiElement createElement(final ASTNode node);

  public void indexStub(final S stub, final IndexSink sink) {
  }

  public String getExternalId() {
    return "gr." + super.toString();
  }

}
