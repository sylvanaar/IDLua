/*
* Copyright 2011 Jon S Akhtar (Sylvanaar)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.sylvanaar.idea.Lua.lang.psi.stubs.elements;

import com.intellij.openapi.diagnostic.*;
import com.intellij.openapi.util.*;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import org.apache.commons.lang.*;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/23/11
 * Time: 8:01 PM
 */
public class LuaStubGlobalDeclarationType extends LuaStubElementType<LuaGlobalDeclarationStub, LuaGlobalDeclaration> {
    private static final Logger log = Logger.getInstance("Lua.StubGlobal");
    public LuaStubGlobalDeclarationType() {
        this("global stub name");
    }

    public LuaStubGlobalDeclarationType(String s) {
        super(s);
    }

    @Override
    public LuaGlobalDeclaration createPsi(LuaGlobalDeclarationStub stub) {
        return new LuaGlobalDeclarationImpl(stub);
    }


    @Override
    public LuaGlobalDeclarationStub createStub(LuaGlobalDeclaration psi, StubElement parentStub) {
        final LuaType luaType = psi.getLuaType();
        final byte[] bytes = luaType instanceof LuaPrimativeType ? null :SerializationUtils.serialize(luaType);
        return new LuaGlobalDeclarationStubImpl(parentStub, StringRef.fromString(psi.getName()),
                psi.getModuleName(), bytes, luaType);
    }

    @Override
    public void serialize(LuaGlobalDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        LuaStubUtils.writeSubstitutableType(stub.getLuaType(), stub.getEncodedType(), dataStream);
        LuaStubUtils.writeNullableString(dataStream, stub.getModule());

    }

    @Override
    public LuaGlobalDeclarationStub deserialize(StubInputStream dataStream, StubElement parentStub) throws
            IOException {

        StringRef ref = dataStream.readName();

        assert ref != null : "Null name in stub stream";

        final Pair<LuaType,byte[]> pair = LuaStubUtils.readSubstitutableType(dataStream);
        byte[] typedata = pair.getSecond();
        LuaType type = pair.first;

        String module = LuaStubUtils.readNullableString(dataStream);

        return new LuaGlobalDeclarationStubImpl(parentStub, ref, module, typedata, type);
    }

    @Override
    public String getExternalId() {
        return "lua.GLOBAL_DEF";
    }

    @Override
    public void indexStub(LuaGlobalDeclarationStub stub, IndexSink sink) {
        String module = stub.getModule();
        final String stubName = stub.getName();

        if (stubName != null) {
            String name = module == null ? stubName : module + "." + stubName;
            log.debug("sink: " + name);
            sink.occurrence(LuaGlobalDeclarationIndex.KEY, name);
        }
    }


}
