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

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/19/11
 * Time: 7:40 PM
 */
public class LuaDebugProcess extends XDebugProcess {
    /**
     * @param session pass <code>session</code> parameter of {@link com.intellij.xdebugger
     * .XDebugProcessStarter#start} method to this constructor
     */
    protected LuaDebugProcess(@NotNull XDebugSession session) {
        super(session);
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startStepOver() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startStepInto() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startStepOut() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resume() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
