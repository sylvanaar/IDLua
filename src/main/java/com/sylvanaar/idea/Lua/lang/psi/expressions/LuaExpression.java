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

package com.sylvanaar.idea.Lua.lang.psi.expressions;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 11:35:57 PM
 */
public interface LuaExpression extends LuaPsiElement {
    public static final Object NIL_VALUE = new Object();

    static final Key<CachedValue<LuaType>> CALCULATED_TYPE = Key.create("Calculated Type");

    LuaExpression[] EMPTY_ARRAY = new LuaExpression[0];
    
    PsiElement replaceWithExpression(LuaExpression newCall, boolean b);

    @NotNull
    LuaType getLuaType();

    void setLuaType(LuaType type);


    @Nullable
    Object evaluate();
}
