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
//package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;
//
//import com.intellij.lang.ASTNode;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.StubBasedPsiElement;
//import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
//import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaCompoundIdentifierStub;
//import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
//
///**
// * Created by IntelliJ IDEA.
// * User: Jon S Akhtar
// * Date: 1/29/11
// * Time: 8:19 PM
// */
//public class LuaCompoundSelfIdentifierImpl extends LuaCompoundIdentifierImpl implements LuaCompoundIdentifier, StubBasedPsiElement<LuaCompoundIdentifierStub> {
//    public LuaCompoundSelfIdentifierImpl(ASTNode node) {
//        super(node);
//    }
//    public LuaCompoundSelfIdentifierImpl(LuaCompoundIdentifierStub stub) {
//        super(stub, LuaElementTypes.GETSELF);
//    }
//
//    public String getOperator() {
//        PsiElement e = findChildByType(LuaElementTypes.COLON);
//
//        return e!=null?e.getText():"err";
//    }
//
//
//}
