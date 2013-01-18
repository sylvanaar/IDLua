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

package com.sylvanaar.idea.Lua.lang.psi.stubs.index;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/23/11
 * Time: 8:27 PM
 */
public class LuaGlobalDeclarationIndex extends StringStubIndexExtension<LuaDeclarationExpression> {
    public static final StubIndexKey<String, LuaDeclarationExpression> KEY =
            StubIndexKey.createIndexKey("lua.global.name");

    private static final LuaGlobalDeclarationIndex ourInstance = new LuaGlobalDeclarationIndex();

    public static LuaGlobalDeclarationIndex getInstance() {
        return ourInstance;
    }

    @Override
    public Collection<LuaDeclarationExpression> get(final String s, final Project project, @NotNull final GlobalSearchScope scope) {
        //return StubIndexImpl.safeGet(KEY, s, project, scope, LuaDeclarationExpression.class);
         return super.get(s, project, scope);
//         return super.get(s, project, new LuaSourceFilterScope(scope));
    }

    @NotNull
    public StubIndexKey<String, LuaDeclarationExpression> getKey() {
        return KEY;
    }
}
