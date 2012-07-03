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

package com.sylvanaar.idea.Lua.lang.psi.stubs.impl;

import com.intellij.psi.stubs.*;
import com.intellij.util.io.*;
import com.sylvanaar.idea.Lua.lang.parser.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/3/11
 * Time: 4:51 AM
 */
public class LuaFieldStub extends NamedStubBase<LuaFieldIdentifier> implements LuaTypedStub{
    private byte[] myType = null;
    private LuaType luaType;

    public LuaFieldStub(StubElement parent, StringRef name, byte[] type, LuaType luaType) {
        super(parent, LuaElementTypes.FIELD_NAME, name);
        myType = type;
        this.luaType = luaType;
    }

    public byte[] getEncodedType() {
        return myType;
    }

    public LuaType getLuaType() {
        return luaType;
    }

}
