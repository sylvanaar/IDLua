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
//import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
//import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
//import com.sylvanaar.idea.Lua.lang.psi.impl.statements.LuaFunctionDefinitionStatementImpl;
//import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaDefStub;
//import com.sylvanaar.idea.Lua.lang.psi.stubs.impl.LuaDefStubImpl;
//
//import java.io.IOException;
//
///**
// * @author ilyas
// */
//public class LuaDefFunctionElementType extends LuaStubElementType<LuaDefStub, LuaDef> {
//
//  public LuaDefFunctionElementType() {
//    super("deffunction");
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
//    return new LuaFunctionDefinitionStatementImpl(node);
//  }
//
//  public LuaDef createPsi(LuaDefStub stub) {
//    return new LuaDefnMethodImpl(stub, LuaElementTypes.DEFMETHOD);
//  }
//
//  public LuaDefStub createStub(LuaDef psi, StubElement parentStub) {
//    return new LuaDefStubImpl(parentStub, StringRef.fromString(psi.getName()), LuaElementTypes.DEFMETHOD);
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
