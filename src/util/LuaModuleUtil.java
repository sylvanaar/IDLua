/*
* Copyright 2010 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.sylvanaar.idea.Lua.module.LuaModuleType;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
import org.jetbrains.annotations.Nullable;

/**
* @author Maxim.Manuylov
*         Date: 29.04.2010
*/
public class LuaModuleUtil {
    public static boolean isLuaSdk(@Nullable final Sdk sdk) {
        return sdk != null && sdk.getSdkType() instanceof LuaSdkType;
    }

    public static boolean isLuaModule(@Nullable final Module module) {
        return module != null && LuaModuleType.ID.equals(module.getModuleType().getId());
    }
}
