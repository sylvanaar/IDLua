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

package com.sylvanaar.idea.Lua.lang.psi.stubs.elements;

import com.intellij.openapi.util.*;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import org.apache.commons.lang.*;
import org.jetbrains.annotations.*;

import java.io.*;

/**
* Created by IntelliJ IDEA.
* User: Jon S Akhtar
* Date: 1/23/11
* Time: 8:01 PM
*/
public class LuaFieldStubType
        extends LuaStubElementType<LuaFieldStub, LuaFieldIdentifier>  implements StubSerializer<LuaFieldStub>  {

    public LuaFieldStubType() {
        super("FIELD");
    }

    @Override
    public LuaFieldIdentifier createPsi(@NotNull LuaFieldStub stub) {
        return new LuaFieldIdentifierImpl(stub);
    }

    @Override
    public LuaFieldStub createStub(@NotNull LuaFieldIdentifier psi, StubElement parentStub) {
        final LuaType luaType = psi.getLuaType();
        final byte[] bytes = luaType instanceof LuaPrimitiveType ? null : SerializationUtils.serialize(luaType);
        return new LuaFieldStub(parentStub, StringRef.fromString(psi.getName()), bytes,
                luaType);
    }

    @Override
    public void serialize(LuaFieldStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        LuaStubUtils.writeSubstitutableType(stub.getLuaType(), stub.getEncodedType(), dataStream);
    }

    @Override
    public LuaFieldStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
        StringRef ref = dataStream.readName();

        final Pair<LuaType, byte[]> pair = LuaStubUtils.readSubstitutableType(dataStream);
        byte[] typedata = pair.getSecond();
        LuaType type = pair.first;

        return new LuaFieldStub(parentStub, ref, typedata, type);
    }

    @Override
    public void indexStub(LuaFieldStub stub, IndexSink sink) {
//        String name = stub.getName();
//
//        if (name != null) {
//          sink.occurrence(LuaFieldIndex.KEY, name);
//        }
    }

}
