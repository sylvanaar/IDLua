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

package com.sylvanaar.idea.Lua.lang.psi.impl.lists;

import com.intellij.lang.ASTNode;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaFunctionArguments;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/17/11
 * Time: 12:40 AM
 */
public class LuaFunctionArgumentsImpl extends LuaPsiElementImpl implements LuaFunctionArguments {
    public LuaFunctionArgumentsImpl(ASTNode node) {
        super(node);
    }

    @Override
    public LuaExpressionList getExpressions() {
        return findChildByClass(LuaExpressionList.class);
    }
}
