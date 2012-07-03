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

package com.sylvanaar.idea.Lua.lang.psi.stubs.api;

import com.intellij.psi.stubs.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/21/11
 * Time: 7:34 PM
 */
public interface LuaCompoundIdentifierStub extends NamedStub<LuaCompoundIdentifier>, LuaTypedStub {
    boolean isGlobalDeclaration();
}
