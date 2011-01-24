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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaGlobalDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaGlobalIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaGlobalDeclarationImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaGlobalIdentifierImpl;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaStubElementType;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaGlobalDeclarationStub;
import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.LuaGlobalDeclarationStubImpl;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/23/11
 * Time: 8:01 PM
 */
public class LuaStubGlobalDeclarationType extends LuaStubElementType<LuaGlobalDeclarationStub, LuaGlobalDeclaration> {
    public LuaStubGlobalDeclarationType() {
        super("global stub name");
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        LuaGlobalIdentifier g= new LuaGlobalIdentifierImpl(node);

        if (g.isDeclaration())
            return new LuaGlobalDeclarationImpl(node);
        
        return g;
    }

    @Override
    public LuaGlobalDeclaration createPsi(LuaGlobalDeclarationStub stub) {
        return new LuaGlobalDeclarationImpl(stub);
    }

    @Override
    public LuaGlobalDeclarationStub createStub(LuaGlobalDeclaration psi, StubElement parentStub) {
        return new LuaGlobalDeclarationStubImpl(parentStub, StringRef.fromString(psi.getName()));
    }

    @Override
    public void serialize(LuaGlobalDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
    }

    @Override
    public LuaGlobalDeclarationStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
        StringRef ref = dataStream.readName();
        return new LuaGlobalDeclarationStubImpl(parentStub, ref);
    }
}
