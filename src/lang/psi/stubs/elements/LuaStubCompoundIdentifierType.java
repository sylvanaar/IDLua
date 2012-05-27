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

import com.intellij.psi.stubs.*;
import com.intellij.util.io.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/21/11
 * Time: 7:32 PM
 */
public class LuaStubCompoundIdentifierType
    extends LuaStubElementType<LuaCompoundIdentifierStub, LuaCompoundIdentifier> {
    public LuaStubCompoundIdentifierType() {
        super("compound id stub name");
    }

    @Override
    public LuaCompoundIdentifier createPsi(LuaCompoundIdentifierStub stub) {
        return new LuaCompoundIdentifierImpl(stub);
    }

    @Override
    public LuaCompoundIdentifierStub createStub(LuaCompoundIdentifier psi, StubElement parentStub) {
        return new LuaCompoundIdentifierStubImpl(parentStub, psi);
    }

    @Override
    public String getExternalId() {
        return "lua.COMPOUND_ID";
    }

    @Override
    public void serialize(LuaCompoundIdentifierStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeShort(stub.getEncodedType().length);
        dataStream.write(stub.getEncodedType());
        dataStream.writeBoolean(stub.isGlobalDeclaration());
    }

    @Override
    public LuaCompoundIdentifierStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
        StringRef ref = dataStream.readName();

        int len = dataStream.readShort();
        byte[] typedata = new byte[len];
        dataStream.read(typedata, 0, len);

        boolean isDeclaration = dataStream.readBoolean();
        return new LuaCompoundIdentifierStubImpl(parentStub, ref, isDeclaration, typedata);
    }

    @Override
    public void indexStub(LuaCompoundIdentifierStub stub, IndexSink sink) {
        String name = stub.getName();

        if (name != null && stub.isGlobalDeclaration()) {
//            System.out.println("indexed: " + name);
          sink.occurrence(LuaGlobalDeclarationIndex.KEY, name);
        }
    }
}
