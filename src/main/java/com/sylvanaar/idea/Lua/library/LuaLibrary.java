/*
 * Copyright 2012 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.library;

import com.intellij.openapi.roots.libraries.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 8/24/12
 * Time: 6:09 PM
 */
public interface LuaLibrary {
    String      LUA_LIBRARY_TYPE_ID       = "Lua";
    String      LUA_LIBRARY_CATEGORY_NAME = "Lua";
    String      LUA_LIBRARY_KIND_ID       = "Lua";
    LibraryKind KIND                      = LibraryKind.create(LUA_LIBRARY_KIND_ID);
}
