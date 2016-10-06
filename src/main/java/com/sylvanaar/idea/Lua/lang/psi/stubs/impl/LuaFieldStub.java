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

import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaTypedStub;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/3/11
 * Time: 4:51 AM
 */
public class LuaFieldStub extends NamedStubBase<LuaFieldIdentifier> implements LuaTypedStub{
    @Nullable
    private byte[]  myType    = null;
    @Nullable
    private LuaType myLuaType = null;

    public LuaFieldStub(StubElement parent, StringRef name, @Nullable byte[] type, @Nullable LuaType luaType) {
        super(parent, LuaElementTypes.FIELD_NAME, name);
        myType = type;
        this.myLuaType = luaType;
    }

    @Nullable public byte[] getEncodedType() {
        return myType;
    }

    @Nullable public LuaType getLuaType() {
        return myLuaType;
    }

}
