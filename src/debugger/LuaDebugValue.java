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

package com.sylvanaar.idea.Lua.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.frame.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 5/15/11
 * Time: 5:34 AM
 */
public class LuaDebugValue extends XValue {
    private static final Logger log = Logger.getInstance("Lua.LuaDebuggerController");

    String myValueAsString;
    String myTypeName;

    LuaDebugValue(String typeName, String stringValue) {
        myValueAsString = stringValue;
        myTypeName = typeName;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        node.setPresentation(AllIcons.Debugger.Value, myTypeName, myValueAsString, false);
    }

    @Override
    public XValueModifier getModifier() {
        return super.getModifier();
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable navigatable) {
        super.computeSourcePosition(
                navigatable);
    }
}
