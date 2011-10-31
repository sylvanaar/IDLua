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

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaModuleExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaModuleExpressionImpl;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaStubElementType;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaStubUtils;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaModuleDeclarationStub;
import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.LuaModuleDeclarationStubImpl;
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.LuaGlobalDeclarationIndex;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/23/11
 * Time: 8:01 PM
 */
public class LuaStubModuleDeclarationType extends LuaStubElementType<LuaModuleDeclarationStub, LuaModuleExpression>  {
    private static final Logger log = Logger.getInstance("Lua.StubGlobal");
    public LuaStubModuleDeclarationType() {
        this("module stub name");
    }

    public LuaStubModuleDeclarationType(String s) {
        super(s);
    }

    @Override
    public LuaModuleExpression createPsi(LuaModuleDeclarationStub stub) {
        return new LuaModuleExpressionImpl(stub);
    }

    @Override
    public LuaModuleDeclarationStub createStub(LuaModuleExpression psi, StubElement parentStub) {

        log.debug(psi.getText());
        return new LuaModuleDeclarationStubImpl(parentStub, StringRef.fromString(psi.getName()),
                StringRef.fromString(psi.getModuleName()));
    }

    @Override
    public void serialize(LuaModuleDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        LuaStubUtils.writeNullableString(dataStream, stub.getModule());
    }

    @Override
    public LuaModuleDeclarationStub deserialize(StubInputStream dataStream, StubElement parentStub) throws
            IOException {
        StringRef ref = dataStream.readName();

        assert ref != null : "Null name in stub stream";
        
        String module = LuaStubUtils.readNullableString(dataStream);

        return new LuaModuleDeclarationStubImpl(parentStub, ref, StringRef.fromString(module));
    }

    @Override
    public void indexStub(LuaModuleDeclarationStub stub, IndexSink sink) {
        String name = stub.getName();

        if (name != null) {
            sink.occurrence(LuaGlobalDeclarationIndex.KEY, name);
        }
    }

    @Override
    public String getExternalId() {
        return "lua.MODULE_DEF";
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        return new LuaModuleExpressionImpl(node);
    }
}
