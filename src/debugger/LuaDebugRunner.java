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

import com.intellij.execution.*;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.*;
import com.intellij.execution.runners.*;
import com.intellij.execution.ui.*;
import com.intellij.openapi.diagnostic.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.*;
import com.intellij.xdebugger.*;
import com.sylvanaar.idea.Lua.run.*;
import org.jetbrains.annotations.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/19/11
 * Time: 6:42 PM
 */
public class LuaDebugRunner extends GenericProgramRunner {
    private static final Logger log = Logger.getInstance("Lua.LuaDebugRunner");

    ExecutionResult executionResult;
    
    private final XDebugProcessStarter processStarter = new XDebugProcessStarter() {
        @NotNull
        @Override
        public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
            return new LuaDebugProcess(session, executionResult);
        }
    };

    @NotNull
    @Override
    public String getRunnerId() {
        return "com.sylvanaar.idea.Lua.debugger.LuaDebugRunner";
    }

    @Override
    public boolean canRun(@NotNull java.lang.String executorId, @NotNull RunProfile profile) {
        if (!(executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) && profile instanceof LuaRunConfiguration))
            return false;

        try {
            ((RunConfiguration) profile).checkConfiguration();
        } catch (RuntimeConfigurationException e) {
            log.warn("Lua Run Configuration Invalid", e);
            return false;
        }

        return true;
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(Project project, RunProfileState state, RunContentDescriptor contentToReuse, ExecutionEnvironment env) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();

        if (log.isDebugEnabled()) log.debug("Starting LuaDebugProcess");

        executionResult = state.execute(env.getExecutor(), this);

        XDebugSession session = XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse,
                processStarter);

        return session.getRunContentDescriptor();
    }
}
