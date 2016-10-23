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

import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XNumericValuePresentation;
import com.intellij.xdebugger.frame.presentation.XRegularValuePresentation;
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 5/15/11
 * Time: 5:34 AM
 */
public class LuaDebugValue extends XValue {
    String myValueAsString;
    String myTypeName;
    final LuaValue myRawValue;
    final Icon myIcon;

    LuaDebugValue(String typeName, String stringValue, Icon icon) {
        myValueAsString = stringValue;
        myTypeName = typeName;
        myRawValue = null;
        myIcon = icon;
    }

    LuaDebugValue(LuaValue rawValue, String stringValue, Icon icon) {
        myRawValue = rawValue;
        myTypeName = rawValue.typename();
        myIcon = icon;
        if (myTypeName.equals("function")) {
            final LuaValue value = myRawValue.checkclosure().call();
            final LuaString luaString = value.checkstring();

            myValueAsString = luaString.toString();
        } else if (myTypeName.equals("table")) {
            final LuaValue luaValue = myRawValue.checktable().get(LuaString.valueOf("_____ID"));
            final LuaString luaString = luaValue.checkstring();

            myValueAsString = luaString.toString();
        } else {
            myValueAsString = rawValue.toString();

        }
    }


    public boolean isString() {
        return myTypeName.equals("string");
    }

    public boolean isNumber() {
        return myTypeName.equals("number");
    }

    public boolean isBool() {
        return myTypeName.equals("boolean");
    }

    public boolean isTable() { return myTypeName.equals("table"); }


    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        XValuePresentation presentation = getPresentation();
        node.setPresentation(myIcon, presentation, isTable());
    }

    @NotNull
    private XValuePresentation getPresentation() {
        final String stringValue = myValueAsString;
        if (isNumber()) return new XNumericValuePresentation(stringValue);
        if (isString()) return new XStringValuePresentation(stringValue);
        if (isBool()) {
            return new XValuePresentation() {
                @Override
                public void renderValue(@NotNull XValueTextRenderer renderer) {
                    renderer.renderValue(stringValue);
                }
            };
        }
        return new XRegularValuePresentation(stringValue, myTypeName);
    }



}
