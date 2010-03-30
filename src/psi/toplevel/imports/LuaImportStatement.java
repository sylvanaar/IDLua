/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.psi.toplevel.imports;

import com.sylvanaar.idea.Lua.psi.auxiliary.modifiers.LuaModifierList;
import com.sylvanaar.idea.Lua.psi.statements.LuaRequireStatement;
import com.sylvanaar.idea.Lua.psi.toplevel.LuaTopStatement;
import com.sylvanaar.idea.Lua.psi.types.LuaCodeReferenceElement;
import org.jetbrains.annotations.Nullable;




/**
 * @author ilyas
 */
public interface LuaImportStatement extends LuaTopStatement {
  public static final LuaRequireStatement[] EMPTY_ARRAY = new LuaRequireStatement[0];
  @Nullable
  LuaCodeReferenceElement getImportReference();

  @Nullable
  String getImportedName();

  boolean isOnDemand();

  boolean isStatic();

  boolean isAliasedImport();

  @Nullable
  LuaModifierList getAnnotationList();
}