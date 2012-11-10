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

package com.sylvanaar.idea.Lua.lang.psi.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubSerializer;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/23/11
 * Time: 8:19 PM
 */
public abstract class LuaStubElementType<S extends StubElement, T extends LuaPsiElement>
        extends IStubElementType<S, T>  implements StubSerializer<S> {

    protected LuaStubElementType(@NonNls @NotNull String debugName) {
        super(debugName, LuaFileType.LUA_LANGUAGE);
    }


    @Override
    public String getExternalId() {
        return getLanguage().getID() + '.' + toString();
    }
}
