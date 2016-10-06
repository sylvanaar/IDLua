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
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaCompoundIdentifierStub;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/21/11
 * Time: 7:33 PM
 */
public class LuaCompoundIdentifierStubImpl extends NamedStubBase<LuaCompoundIdentifier>
        implements LuaCompoundIdentifierStub {

    private static final byte[] EMPTY_TYPE = new byte[0];
    private final boolean isGlobalDeclaration;
    private byte[] myType = null;
    private LuaType luaType;

    public LuaCompoundIdentifierStubImpl(StubElement parent, StringRef name, boolean isDeclaration, byte[] type,
                                         LuaType luaType) {
        super(parent, LuaElementTypes.GETTABLE, name);
        this.isGlobalDeclaration = isDeclaration;
        myType = type;
        this.luaType = luaType;
    }

    @Override
    public boolean isGlobalDeclaration() {
        return isGlobalDeclaration;
    }

    @Override
    public byte[] getEncodedType() {
        return myType;
    }

    @Override
    public LuaType getLuaType() {
        return luaType;
    }


}
