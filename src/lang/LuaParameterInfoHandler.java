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

package com.sylvanaar.idea.Lua.lang;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameterList;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 19, 2010
 * Time: 6:01:15 AM
 */
public class LuaParameterInfoHandler implements ParameterInfoHandler<LuaParameterList, LuaParameter> {

    @Override
    public boolean couldShowInLookup() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object[] getParametersForDocumentation(LuaParameter p, ParameterInfoContext context) {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuaParameterList findElementForParameterInfo(CreateParameterInfoContext context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void showParameterInfo(@NotNull LuaParameterList element, CreateParameterInfoContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuaParameterList findElementForUpdatingParameterInfo(UpdateParameterInfoContext context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateParameterInfo(@NotNull LuaParameterList o, UpdateParameterInfoContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getParameterCloseChars() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean tracksParameterIndex() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateUI(LuaParameter p, ParameterInfoUIContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
