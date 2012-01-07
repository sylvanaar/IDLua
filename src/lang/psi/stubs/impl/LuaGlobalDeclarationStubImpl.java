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

package com.sylvanaar.idea.Lua.lang.psi.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaGlobalDeclarationStub;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalDeclaration;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/23/11
 * Time: 8:10 PM
 */
public class LuaGlobalDeclarationStubImpl extends NamedStubBase<LuaGlobalDeclaration> implements LuaGlobalDeclarationStub {
    private String myModule;
    private byte[] myType;

//    public LuaGlobalDeclarationStubImpl(LuaGlobalDeclaration e) {
//        this(null, LuaElementTypes.GLOBAL_NAME_DECL,
//                StringRef.fromString(e.getName()),
//                StringRef.fromString(e.getModuleName()));
//    }

    public LuaGlobalDeclarationStubImpl(@Nullable StubElement parent, IStubElementType elementType, StringRef name, String module, byte[] type) {
        super(parent, elementType, name);
        myModule = module;
        myType = type;
    }

    public LuaGlobalDeclarationStubImpl(StubElement parent, StringRef name, String module, byte[] type) {
        this(parent, LuaElementTypes.GLOBAL_NAME_DECL, name, module, type);
    }

    @Override
    @Nullable
    public String getModule() {
        if (myModule == null) return null;
        return myModule;
    }

    public byte[] getEncodedType() {
        return myType;
    }
}
