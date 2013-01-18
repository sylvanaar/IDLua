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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaModuleExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaModuleExpressionImpl;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaStubElementType;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaModuleDeclarationStub;
import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.LuaModuleDeclarationStubImpl;
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.LuaGlobalDeclarationIndex;
import org.apache.commons.lang.SerializationUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/23/11
 * Time: 8:01 PM
 */
public class LuaStubModuleDeclarationType extends LuaStubElementType<LuaModuleDeclarationStub, LuaModuleExpression>  {
    private static final Logger log = Logger.getInstance("Lua.StubModule");
    public LuaStubModuleDeclarationType() {
        this("MODULE");
    }

    public LuaStubModuleDeclarationType(String s) {
        super(s);
    }

    @Override public String getExternalId() {
        return "Lua.MODULE";
    }

    @Override
    public LuaModuleExpression createPsi(@NotNull LuaModuleDeclarationStub stub) {
        return new LuaModuleExpressionImpl(stub);
    }

    @Override
    public LuaModuleDeclarationStub createStub(@NotNull LuaModuleExpression psi, StubElement parentStub) {

        log.debug(psi.getText());
        final String moduleName = psi.getModuleName();
        return new LuaModuleDeclarationStubImpl(parentStub, StringRef.fromNullableString(psi.getName()), StringRef.fromNullableString(moduleName),
                SerializationUtils.serialize(psi.getLuaType()));
    }

    @Override
    public void serialize(LuaModuleDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getModule());
        dataStream.writeVarInt(stub.getEncodedType().length);
        dataStream.write(stub.getEncodedType());
    }

    @Override
    public LuaModuleDeclarationStub deserialize(StubInputStream dataStream, StubElement parentStub) throws
            IOException {
        StringRef ref = dataStream.readName();
        StringRef mref = dataStream.readName();

        int len = dataStream.readVarInt();
        byte[] typedata = new byte[len];
        int readLen = dataStream.read(typedata, 0, len);

        assert readLen == len;

        return new LuaModuleDeclarationStubImpl(parentStub, ref, mref, typedata);
    }

    @Override
    public void indexStub(LuaModuleDeclarationStub stub, IndexSink sink) {
        final String module = stub.getModule();
        final String stubName = stub.getName();

        if (StringUtil.isNotEmpty(stubName)) {
            String name = StringUtil.isEmpty(module) ? stubName : module + '.' + stubName;
            log.debug("sink: " + name);
            sink.occurrence(LuaGlobalDeclarationIndex.KEY, name);
        }
    }


}
