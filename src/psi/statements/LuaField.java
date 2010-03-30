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

import com.intellij.psi.PsiField;
import com.intellij.psi.StubBasedPsiElement;
import com.sylvanaar.idea.Lua.psi.statements.typedef.members.LuaAccessorMethod;
import com.sylvanaar.idea.Lua.psi.statements.typedef.members.LuaMember;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author ven
 */
public interface LuaField extends LuaVariable, LuaMember, PsiField,LuaTopLevelDefintion /* TODO, StubBasedPsiElement<LuaFieldStub> LuaDocCommentOwner */ {
  public static final LuaField[] EMPTY_ARRAY = new LuaField[0];

  boolean isProperty();

  @Nullable
  LuaAccessorMethod getSetter();

  @NotNull
 LuaAccessorMethod[] getGetters();

  @NotNull
  public Set<String>[] getNamedParametersArray();
}
