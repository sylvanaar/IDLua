package com.sylvanaar.idea.Lua.debugger;

import com.intellij.icons.AllIcons;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jon on 10/22/2016.
 */
public class LuaRemoteStack {

    private final LuaTable stack;

    private LuaRemoteStack(LuaTable stack) {
        this.stack = stack;
    }

    public static LuaRemoteStack create(String code) {
        Globals globals = JsePlatform.debugGlobals();
        LuaValue chunk = globals.load(code);
        LuaTable stackDump = chunk.call().checktable();
        stackDump.set(LuaString.valueOf("_____ID"), LuaValue.NIL);
        return new LuaRemoteStack(stackDump);
    }

    public List<LuaDebugVariable> getLocals(int frame) {
        final LuaTable stackFrame = getStackFrame(frame);

        final List<LuaDebugVariable> result = new LinkedList<LuaDebugVariable>();
        final LuaTable localsAtLevel = stackFrame.get(2).checktable();
        localsAtLevel.set(LuaString.valueOf("_____ID"), LuaValue.NIL);
        for (LuaValue key : localsAtLevel.keys()) {
            final LuaTable variableInfo = localsAtLevel.get(key).checktable();
            result.add(convertVariable(key, variableInfo));
        }

        return result;
    }

    public List<LuaDebugVariable> getUpvalues(int frame) {
        final LuaTable stackFrame = getStackFrame(frame);

        final List<LuaDebugVariable> result = new LinkedList<LuaDebugVariable>();
        final LuaTable upvaluesAtLevel = stackFrame.get(3).checktable();
        upvaluesAtLevel.set(LuaString.valueOf("_____ID"), LuaValue.NIL);

        for (LuaValue key : upvaluesAtLevel.keys()) {
            final LuaTable variableInfo = upvaluesAtLevel.get(key).checktable();
            result.add(convertVariable(key, variableInfo));
        }

        return result;
    }


    private LuaTable getStackFrame(int frame) {
        final int index = stack.keyCount() - (frame);
        final LuaValue stackValueAtLevel = stack.get(index);

        stackValueAtLevel.set(LuaString.valueOf("_____ID"), LuaValue.NIL);
        return stackValueAtLevel.checktable();
    }

    private LuaDebugVariable convertVariable(LuaValue key, LuaTable variableInfo) {
        final LuaValue rawValue = variableInfo.get(1);
        final LuaString debugText = variableInfo.get(2).checkstring();
        final LuaDebugValue debugValue = new LuaDebugValue(rawValue, debugText.tojstring(), AllIcons.Nodes.Variable);
        return new LuaDebugVariable(
                key.toString(),
                debugValue
        );
    }
}
