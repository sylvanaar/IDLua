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
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaFileStubBuilder;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaFileStub;
import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.LuaFileStubImpl;

import java.io.IOException;

/**
 * @author ilyas
 */
public class LuaStubFileElementType extends IStubFileElementType<LuaFileStub> {
  private static final int CACHES_VERSION = 11;

  public LuaStubFileElementType() {
    super(LuaFileType.LUA_LANGUAGE);
  }

  public StubBuilder getBuilder() {
    return new LuaFileStubBuilder();
  }

  @Override
  public int getStubVersion() {
    return super.getStubVersion() + CACHES_VERSION;
  }

  public String getExternalId() {
    return "Lua.FILE";
  }

  @Override
  public void indexStub(PsiFileStub stub, IndexSink sink) {
    super.indexStub(stub, sink);
  }

  @Override
  public void serialize(final LuaFileStub stub, final StubOutputStream dataStream) throws IOException {

    dataStream.writeName(stub.getName().toString());

  }

  @Override
  public LuaFileStub deserialize(final StubInputStream dataStream, final StubElement parentStub) throws IOException {
    StringRef packName = dataStream.readName();
    StringRef name = dataStream.readName();
    boolean isScript = dataStream.readBoolean();
    return new LuaFileStubImpl(packName, name, isScript);
  }

  public void indexStub(LuaFileStub stub, IndexSink sink) {
//    String name = stub.getName().toString();
//    if (stub.isClassDefinition() && name != null) {
//      sink.occurrence(LuaClassNameIndex.KEY, name);
//      final String pName = stub.getPackageName().toString();
//      final String fqn = pName == null || pName.length() == 0 ? name : pName + "." + name;
//      sink.occurrence(LuaFullScriptNameIndex.KEY, fqn.hashCode());
//    }
  }

}