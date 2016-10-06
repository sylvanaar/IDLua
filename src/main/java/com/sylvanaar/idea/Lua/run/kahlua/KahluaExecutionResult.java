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

package com.sylvanaar.idea.Lua.run.kahlua;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.actionSystem.AnAction;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 6, 2010
 * Time: 8:51:55 AM
 */
public class KahluaExecutionResult  implements ExecutionResult {
    private final ExecutionConsole myConsole;
    private final AnAction[] myActions;

    public KahluaExecutionResult(final ExecutionConsole console, final AnAction... actions) {
        this.myActions = actions;
        this.myConsole = console;
    }

    @Override
    public ExecutionConsole getExecutionConsole() {
        return myConsole;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AnAction[] getActions() {
        return myActions;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProcessHandler getProcessHandler() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
