/*
 * Copyright 2012 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.findUsages;

import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import com.sylvanaar.idea.Lua.LuaBundle;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/8/12
 * Time: 8:37 AM
 */
public class LuaUsageTypeProvider implements UsageTypeProvider {
    private static final UsageType LUA_USAGE_TYPE = new UsageType(LuaBundle.message("lua.usages.type"));
    private static final UsageType LUADOC_USAGE_TYPE = new UsageType(LuaBundle.message("luadoc.usages.type"));

    @Override
    public UsageType getUsageType(PsiElement element) {
        if (element instanceof LuaDocPsiElement)
            return LUADOC_USAGE_TYPE;

        if (element instanceof LuaPsiElement)
            return LUA_USAGE_TYPE;

        return null;
    }
}
