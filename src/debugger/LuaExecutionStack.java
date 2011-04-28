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

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/28/11
 * Time: 11:06 AM
 */
public class LuaExecutionStack extends XExecutionStack {

    LuaStackFrame myTopFrame;

    public LuaExecutionStack(String displayName) {
        super(displayName);
    }

    public LuaExecutionStack(String displayName, LuaStackFrame topFrame) {
        super(displayName);

        myTopFrame = topFrame;
    }


    @Override
    public XStackFrame getTopFrame() {
        return myTopFrame;
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
