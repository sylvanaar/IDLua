///*
// * Copyright 2010 Jon S Akhtar (Sylvanaar)
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
//package com.sylvanaar.idea.Lua.lang.psi.impl;
//
//import com.intellij.lang.ASTNode;
//import com.intellij.navigation.ItemPresentation;
//import com.intellij.psi.PsiElement;
//import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
//import com.sylvanaar.idea.Lua.lang.psi.LuaFunction;
//import com.sylvanaar.idea.Lua.lang.psi.LuaIdentifier;
//
///**
// * Created by IntelliJ IDEA.
// * User: Jon S Akhtar
// * Date: Apr 14, 2010
// * Time: 2:32:36 AM
// */
//public class LuaFunctionImpl extends LuaPsiElementImpl implements LuaFunction {
//
//
//    LuaIdentifier identifier = null;
//
//
//    LuaParameterListImpl parameters = null;
//
//    public LuaFunctionImpl(ASTNode node) {
//        super(node);
//    }
//
//    @Override
//    public LuaIdentifier getIdentifier() {
//        if (identifier  == null) {
//        PsiElement e = findChildByType(LuaElementTypes.FUNCTION_IDENTIFIER);
//        if (e != null)
//            identifier = (LuaIdentifierImpl) e;
//        }
//        return identifier;
//    }
//
//
//    public LuaParameterListImpl getParameters() {
//        if (parameters  == null) {
//        PsiElement e = findChildByType(LuaElementTypes.PARAMETER_LIST);
//        if (e != null)
//            parameters = (LuaParameterListImpl) e;
//        }
//        return parameters;
//    }
//
//  public ItemPresentation getPresentation() {
//
//    return null;
//  }
//
//
//
//    @Override
//    public String toString() {
//        return "Function Declaration";
//    }
//
//}
