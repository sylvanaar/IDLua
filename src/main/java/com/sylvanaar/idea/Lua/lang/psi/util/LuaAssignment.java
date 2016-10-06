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

package com.sylvanaar.idea.Lua.lang.psi.util;

import com.intellij.openapi.util.text.StringUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 6/8/11
 * Time: 12:16 PM
 */
public class LuaAssignment {
    public static final LuaAssignment[] EMPTY_ARRAY = new LuaAssignment[0];

    @NotNull
    public LuaSymbol getSymbol() {
        return mySymbol;
    }

    @NotNull
    public LuaExpression getValue() {
        return myValue;
    }


    @Override
    public String toString() {
        return StringUtil.notNullize(mySymbol.toString()) + " = " + StringUtil.notNullize(myValue.toString());
    }

    LuaSymbol mySymbol;
    LuaExpression myValue;

    public LuaAssignment(LuaSymbol mySymbol, LuaExpression myValue) {
        if (mySymbol instanceof LuaReferenceElement)
            this.mySymbol = (LuaSymbol) ((LuaReferenceElement) mySymbol).getElement();
        else
            this.mySymbol = mySymbol;
        this.myValue = myValue;
    }

    public static LuaExpression FindAssignmentForSymbol(LuaAssignment[] assignments, LuaSymbol symbol) {
        for (LuaAssignment assignment : assignments) {
            if (assignment.getSymbol() == symbol)
                return assignment.getValue();

            if (assignment.getSymbol() instanceof LuaReferenceElement)
                if (((LuaReferenceElement) assignment.getSymbol()).getElement() == symbol)
                    return assignment.getValue();
        }

        return null;
    }
}
