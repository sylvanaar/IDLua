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
import com.intellij.psi.stubs.StubBase;
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
public class LuaGlobalDeclarationStubImpl extends StubBase<LuaGlobalDeclaration> implements LuaGlobalDeclarationStub {

    private final StringRef myName;
    private StringRef myModule;


    public LuaGlobalDeclarationStubImpl(LuaGlobalDeclaration e) {
        this(null, LuaElementTypes.GLOBAL_NAME_DECL,
                StringRef.fromString(e.getName()),
                StringRef.fromString(e.getModuleName()));
    }

    public LuaGlobalDeclarationStubImpl(@Nullable StubElement parent, IStubElementType type, StringRef name, StringRef module) {
        super(parent, type);
        myName = name;
        myModule = module;

        assert myName != null : "Invalid Stub Created";
    }

    public LuaGlobalDeclarationStubImpl(StubElement parent, StringRef name, StringRef module) {
        this(parent, LuaElementTypes.GLOBAL_NAME_DECL, name, module);
    }

    @Override
    @Nullable
    public String getModule() {
        if (myModule == null) return null;
        return myModule.getString();
    }

    @Override
    public String getName() {
        if (myModule == null)
            return myName.getString();

        return myModule.getString() + "." + myName.getString();
    }
}
