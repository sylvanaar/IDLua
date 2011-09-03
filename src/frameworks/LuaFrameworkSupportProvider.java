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

package com.sylvanaar.idea.Lua.frameworks;

import com.intellij.ide.util.frameworkSupport.FrameworkSupportProviderBase;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 8/22/11
 * Time: 9:05 PM
 */
public class LuaFrameworkSupportProvider extends FrameworkSupportProviderBase {
    protected LuaFrameworkSupportProvider(@org.jetbrains.annotations.NonNls @NotNull String id,
                                          @NotNull String title) {
        super(id, title);
    }

    @Override
    protected void addSupport(@NotNull Module module, @NotNull ModifiableRootModel rootModel, FrameworkVersion version,
                              @Nullable Library library) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isEnabledForModuleType(@NotNull ModuleType moduleType) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
