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

import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/28/11
 * Time: 11:18 AM
 */
public class LuaSuspendContext extends XSuspendContext {
    XBreakpoint myBreakpoint = null;
    Project myProject = null;
    String myEncodedStack;


    public LuaSuspendContext(Project p, XBreakpoint bp, String stack) {
        myBreakpoint = bp;
        myProject = p;
        myEncodedStack = stack!=null?stack:"";
    }

    @Override
    public XExecutionStack getActiveExecutionStack() {

        XSourcePosition position = myBreakpoint.getSourcePosition();

        LuaStackFrame frame = new LuaStackFrame(myProject, position);

        return new LuaExecutionStack(myProject, "simple stack", frame, myEncodedStack);
    }
}
