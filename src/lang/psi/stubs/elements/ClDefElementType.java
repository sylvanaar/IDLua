///*
// * Copyright 2011 Jon S Akhtar (Sylvanaar)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.sylvanaar.idea.Lua.lang.psi.stubs.elements;
//
//import com.intellij.lang.ASTNode;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.stubs.IndexSink;
//import com.intellij.psi.stubs.StubElement;
//import com.intellij.psi.stubs.StubInputStream;
//import com.intellij.psi.stubs.StubOutputStream;
//import com.intellij.util.io.StringRef;
//import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaDefStub;
//import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.LuaDefStubImpl;
//import org.jetbrains.plugins.Lua.parser.LuaElementTypes;
//import org.jetbrains.plugins.Lua.psi.api.defs.ClDef;
//import org.jetbrains.plugins.Lua.psi.stubs.api.ClDefStub;
//import org.jetbrains.plugins.Lua.psi.stubs.index.ClDefNameIndex;
//
//import java.io.IOException;
//
///**
// * @author ilyas
// */
//public class LuaDefElementType extends LuaStubElementType<ClDefStub, LuaDef> {
//
//  public LuaDefElementType() {
//    super("def-element");
//  }
//
//  public void serialize(LuaDefStub stub, StubOutputStream dataStream) throws IOException {
//    dataStream.writeName(stub.getName());
//  }
//
//  public LuaDefStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
//    StringRef ref = dataStream.readName();
//    return new LuaDefStubImpl(parentStub, ref, this);
//  }
//
//  public PsiElement createElement(ASTNode node) {
//    return new LuaDefImpl(node);
//  }
//
//  public LuaDef createPsi(ClDefStub stub) {
//    return new LuaDefImpl(stub, LuaElementTypes.DEF);
//  }
//
//  public LuaDefStub createStub(ClDef psi, StubElement parentStub) {
//    return new LuaDefStubImpl(parentStub, StringRef.fromString(psi.getName()), LuaElementTypes.DEF);
//  }
//
//  @Override
//  public void indexStub(ClDefStub stub, IndexSink sink) {
//    final String name = stub.getName();
//    if (name != null) {
//      sink.occurrence(ClDefNameIndex.KEY, name);
//    }
//  }
//}
