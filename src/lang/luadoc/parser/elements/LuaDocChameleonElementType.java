/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.luadoc.parser.elements;

import com.intellij.lang.Language;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.sylvanaar.idea.Lua.LuaFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class LuaDocChameleonElementType extends ILazyParseableElementType {
  public LuaDocChameleonElementType(@NonNls String debugName) {
    super(debugName);
  }

  @NotNull
  public Language getLanguage() {
    return LuaFileType.LUA_FILE_TYPE.getLanguage();
  }

}
