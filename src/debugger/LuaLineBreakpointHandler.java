package com.sylvanaar.idea.Lua.debugger;

import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import org.jetbrains.annotations.NotNull;

public class LuaLineBreakpointHandler extends XBreakpointHandler {
    protected LuaDebugProcess myDebugProcess;



    public LuaLineBreakpointHandler(LuaDebugProcess debugProcess) {
        super(LuaLineBreakpointType.class);
        myDebugProcess = debugProcess;
    }

    public void registerBreakpoint(@NotNull XBreakpoint xBreakpoint) {
        myDebugProcess.addBreakPoint(xBreakpoint);
    }

    public void unregisterBreakpoint(@NotNull XBreakpoint xBreakpoint, boolean temporary) {        
        myDebugProcess.removeBreakPoint(xBreakpoint);
    }

    

}
