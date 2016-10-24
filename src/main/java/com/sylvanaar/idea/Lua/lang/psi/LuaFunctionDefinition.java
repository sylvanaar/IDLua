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

package com.sylvanaar.idea.Lua.lang.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocCommentOwner;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaParameterList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlockStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaFunction;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaBlockVariablesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 11, 2010
 * Time: 3:32:19 PM
 */
public interface LuaFunctionDefinition extends LuaPsiElement, LuaBlockStatement, ItemPresentation, LuaExpression, LuaDocCommentOwner, LuaBlockVariablesProvider {
    @Override
    @Nullable
    String getName();

    @Nullable
    LuaSymbol getIdentifier();

    LuaParameterList getParameters();

    TextRange getRangeEnclosingBlock();

    @Nullable
    LuaParameter getImpliedSelf();

    @Override
    @NotNull
    LuaFunction getLuaType();
}
