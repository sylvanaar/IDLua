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

import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.*;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.util.io.StringRef;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaFileStub;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaFileStubBuilder;

import java.io.IOException;


public class LuaStubFileElementType extends IStubFileElementType<LuaFileStub> implements StubSerializer<LuaFileStub> {
    private static final int CACHES_VERSION = 19;

    public LuaStubFileElementType() {
        super("FILE", LuaFileType.LUA_LANGUAGE);
    }

    @Override
    public StubBuilder getBuilder() {
        return new LuaFileStubBuilder();
    }

    @Override
    public int getStubVersion() {
        return super.getStubVersion() + CACHES_VERSION;
    }

    @Override
    public String getExternalId() {
        return getLanguage().getID()+ '.' +toString();
    }

    @Override
    public void serialize(LuaFileStub stub, StubOutputStream dataStream) throws IOException {
        assert stub != null;
//        System.out.println("serialize: " + stub.getName().getString());
        dataStream.writeName(stub.getName());
    }

    @Override
    public LuaFileStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
        StringRef name = dataStream.readName();

       // System.out.println("deserialized file " + name.getString() + " module " + (module!=null?module.getString():"null"));

        return new LuaFileStub(name);
    }

    @Override
    public void indexStub(LuaFileStub stub, IndexSink sink) {
//        String name = stub.getName();
//        if (name != null) {
//            sink.occurrence(LuaFullScriptNameIndex.KEY, name.hashCode());
//        }
    }

}