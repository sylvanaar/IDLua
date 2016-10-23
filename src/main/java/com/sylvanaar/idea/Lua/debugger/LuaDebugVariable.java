/*
 * Copyright 2016 Jon S Akhtar (Sylvanaar)
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
import com.intellij.util.ThreeState;
import com.intellij.xdebugger.frame.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class LuaDebugVariable extends XNamedValue {
    private final LuaDebugVariable parent;
    final LuaDebugValue value;
    final String type;
    private final boolean isIndex;

    public LuaDebugVariable(String name, LuaDebugValue value) {
        this(name, null, value, false);
    }

    public LuaDebugVariable(String name, LuaDebugVariable parent, LuaDebugValue value, boolean iaIndex) {
        super(name);
        this.parent = parent;
        this.value = value;
        this.type = value.myTypeName;
        this.isIndex = iaIndex;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (value.isTable()) {
            LuaTable myTable = value.myRawValue.checktable();
            final XValueChildrenList xValues = new XValueChildrenList(myTable.keyCount());
            for (LuaValue key : myTable.keys()) {
                if (key.equals(LuaString.valueOf("_____ID"))) continue;
                final LuaValue rawValue = myTable.get(key);
                final LuaDebugValue debugValue = new LuaDebugValue(rawValue, "", AllIcons.Nodes.Field);
                final LuaDebugVariable v = new LuaDebugVariable(key.toString(), this, debugValue, key.isint());
                xValues.add(v.getName(), v);
            }
            node.addChildren(xValues, true);
        } else {
            super.computeChildren(node);
        }
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        value.computePresentation(node, place);
    }

    @NotNull
    @Override
    public ThreeState computeInlineDebuggerData(@NotNull XInlineDebuggerDataCallback callback) {
        return ThreeState.YES;
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable navigatable) {
        super.computeSourcePosition(navigatable);
    }

    @Nullable
    @Override
    public String getEvaluationExpression() {
        if (isIndex) {
            return parent.getName() + "[" + getName() + "]";
        }
        return parent != null ? parent.getName() + "[\"" + getName() + "\"]" : getName();
    }
}
