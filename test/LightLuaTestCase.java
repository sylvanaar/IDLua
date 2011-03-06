/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sylvanaar.idea.Lua.module.LuaModuleType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public abstract class LightLuaTestCase extends LightCodeInsightFixtureTestCase {
  public static final LightProjectDescriptor LUA_DESCRIPTOR = new LightProjectDescriptor() {
    @Override
    public ModuleType getModuleType() {
      return LuaModuleType.getInstance();
    }

    @Override
    public Sdk getSdk() {
      return null; // TODO
    }

    @Override
    public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
//      final Library.ModifiableModel modifiableModel = model.getModuleLibraryTable().createLibrary("GROOVY").getModifiableModel();
//      final VirtualFile groovyJar =
//        JarFileSystem.getInstance().refreshAndFindFileByPath(TestUtils.getMockGroovy1_8LibraryName()+"!/");
//      modifiableModel.addRoot(groovyJar, OrderRootType.CLASSES);
//      modifiableModel.commit();
    }
  };

  @Override
  @NotNull
  protected LightProjectDescriptor getProjectDescriptor() {
    return LUA_DESCRIPTOR;
  }

  /**
   * Return relative path to the test data. Path is relative to the
   * {@link com.intellij.openapi.application.PathManager#getHomePath()}
   *
   * @return relative path to the test data.
   */
  @Override
  @NonNls
  protected abstract String getBasePath();

}
