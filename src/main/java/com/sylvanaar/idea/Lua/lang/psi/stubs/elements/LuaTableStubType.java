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

package com.sylvanaar.idea.Lua.lang.psi.stubs.elements;

import com.intellij.psi.stubs.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import org.apache.commons.lang.*;
import org.jetbrains.annotations.*;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/25/12
 * Time: 6:41 AM
 */

public class LuaTableStubType extends LuaStubElementType<LuaTableStub, LuaTableConstructor> implements
        StubSerializer<LuaTableStub> {

    public LuaTableStubType() {
        super("TABLE");
    }

    @Override
    public LuaTableConstructor createPsi(@NotNull LuaTableStub stub) {
        return new LuaTableConstructorImpl(stub);
    }

    @Override
    public LuaTableStub createStub(@NotNull LuaTableConstructor psi, StubElement parentStub) {
        assert psi.getLuaType() instanceof LuaTable;
//        if (((LuaTable) psi.getLuaType()).getFieldSet().size() > 0)
            return new LuaTableStubImpl(parentStub, SerializationUtils.serialize(psi.getLuaType()));

//        return new LuaTableStubImpl(parentStub);
    }


    @Override
    public void serialize(LuaTableStub stub, StubOutputStream dataStream) throws IOException {
        final byte[] encodedType = stub.getEncodedType();
        final boolean hasType = encodedType != null;
        dataStream.writeBoolean(hasType);
        if (hasType) {
            dataStream.writeVarInt(encodedType.length);
            dataStream.write(encodedType);
        }
    }

    @Override
    @Nullable
    public LuaTableStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
        boolean hasType = dataStream.readBoolean();
        byte[] typedata = null;
        if (hasType)
        {
            int len = dataStream.readVarInt();
            if (len < 0) ((SerializationManagerEx) SerializationManagerEx.getInstance()).repairNameStorage();

            if (len <= 0) {
                return new LuaTableStubImpl(parentStub);
            }

            typedata = new byte[len];
            dataStream.read(typedata, 0, len);
        }

        return new LuaTableStubImpl(parentStub, typedata);
    }

    @Override
    public void indexStub(LuaTableStub stub, IndexSink sink) {

    }
}

