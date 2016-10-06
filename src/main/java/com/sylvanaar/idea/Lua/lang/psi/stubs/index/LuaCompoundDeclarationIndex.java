///*
// * Copyright 2011 Jon S Akhtar (Sylvanaar)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package com.sylvanaar.idea.Lua.lang.psi.stubs.index;
//
//import com.intellij.psi.stubs.StringStubIndexExtension;
//import com.intellij.psi.stubs.StubIndexKey;
//import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
//import org.jetbrains.annotations.*;
//
///**
// * Created by IntelliJ IDEA.
// * User: Jon S Akhtar
// * Date: 1/23/11
// * Time: 8:27 PM
// */
//public class LuaCompoundDeclarationIndex extends StringStubIndexExtension<LuaDeclarationExpression> {
//    public static final StubIndexKey<String, LuaDeclarationExpression> KEY =
//            StubIndexKey.createIndexKey("lua.gettable.name");
//
//    private static final LuaCompoundDeclarationIndex ourInstance = new LuaCompoundDeclarationIndex();
//
//    public static LuaCompoundDeclarationIndex getInstance() {
//        return ourInstance;
//    }
//
//    @NotNull
//    public StubIndexKey<String, LuaDeclarationExpression> getKey() {
//        return KEY;
//    }
//}
