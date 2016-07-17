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
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 5/15/11
 * Time: 5:07 AM
 */
public class LuaDebuggerEvaluator extends XDebuggerEvaluator {
    private static final Logger log = Logger.getInstance("Lua.LuaDebuggerEvaluator");

    private Project myProject;
    private LuaStackFrame luaStackFrame;
    private LuaDebuggerController myController;

    public LuaDebuggerEvaluator(Project myProject, LuaStackFrame luaStackFrame, LuaDebuggerController myController) {

        this.myProject = myProject;
        this.luaStackFrame = luaStackFrame;
        this.myController = myController;
    }

    @Override
    public void evaluate(@NotNull String expression, XEvaluationCallback callback,
                         @Nullable XSourcePosition expressionPosition) {
        log.debug("evaluating: " + expression);
        final XEvaluationCallback evalCallback = callback;
        Promise<LuaDebugValue> promise = myController.execute("return " + expression);
        promise.done(new Consumer<LuaDebugValue>() {
            @Override
            public void consume(LuaDebugValue luaDebugValue) {
                evalCallback.evaluated(luaDebugValue);
            }
        });
    }
}
