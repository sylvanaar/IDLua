/*
 * Copyright 2013 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.sylvanaar.idea.Lua;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.sylvanaar.idea.Lua.sdk.LuaJSdk;

class LuaLightProjectDescriptor extends DefaultLightProjectDescriptor {
    public static final LuaLightProjectDescriptor INSTANCE = new LuaLightProjectDescriptor();

    protected LuaLightProjectDescriptor() {}

    @Override
    public Sdk getSdk() {
        return LuaJSdk.createMockSdk("", "TestSDK");
    }

//  @Override
//  public ModuleType getModuleType() {
//    return LuaModuleType.getInstance();
//  }

    @Override
    public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
        model.inheritSdk();
        model.commit();
    }
}
