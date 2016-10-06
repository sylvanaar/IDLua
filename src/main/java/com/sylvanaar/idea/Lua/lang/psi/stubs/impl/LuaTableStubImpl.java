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

package com.sylvanaar.idea.Lua.lang.psi.stubs.impl;

import com.intellij.psi.stubs.*;
import com.sylvanaar.idea.Lua.lang.parser.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/25/12
 * Time: 6:49 AM
 */
public class LuaTableStubImpl extends StubBase<LuaTableConstructor> implements LuaTableStub {
    public LuaTableStubImpl(StubElement parent) {
        super(parent, LuaElementTypes.TABLE_CONSTUCTOR);
    }

    private byte[] myType;


    public LuaTableStubImpl(StubElement parent, byte[] type) {
        this(parent);
        myType = type;
    }

    public byte[] getEncodedType() {
        return myType;
    }
}
