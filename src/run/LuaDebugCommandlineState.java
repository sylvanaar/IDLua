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

package com.sylvanaar.idea.Lua.run;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.text.StringUtil;
import com.sylvanaar.idea.Lua.sdk.StdLibrary;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 5/26/11
 * Time: 1:58 AM
 */
public class LuaDebugCommandlineState extends LuaCommandLineState {
    public LuaDebugCommandlineState(LuaRunConfiguration runConfiguration, ExecutionEnvironment env) {
        super(runConfiguration, env);
    }

    protected GeneralCommandLine generateCommandLine() {
        GeneralCommandLine commandLine = new GeneralCommandLine();

        if (!StringUtil.isEmptyOrSpaces(getRunConfiguration().getInterpreterPath()))
            commandLine.setExePath(getRunConfiguration().getInterpreterPath());

        // '%s -e "package.path=%s" -l debug %s'
        // TODO: can we use any of the arguments? commandLine.getParametersList().addParametersString(getRunConfiguration().getInterpreterOptions());

        String remDebugPath = StdLibrary.getDebugModuleLocation().getPath();
        commandLine.getParametersList().addParametersString("-e");
        commandLine.getParametersList().add("\"package.path=[[" + remDebugPath + "/?.lua;]]..package.path\"");
        commandLine.getParametersList().addParametersString("-l remdebug");

        if (!StringUtil.isEmptyOrSpaces(getRunConfiguration().getScriptName())) {
            commandLine.addParameter(getRunConfiguration().getScriptName());
        }

        commandLine.getParametersList().addParametersString(getRunConfiguration().getScriptParameters());

        if (!StringUtil.isEmptyOrSpaces(getRunConfiguration().getWorkingDirectory())) {
            commandLine.setWorkDirectory(getRunConfiguration().getWorkingDirectory());
        }

        commandLine.setEnvParams(getRunConfiguration().getEnvs());
        commandLine.setPassParentEnvs(getRunConfiguration().isPassParentEnvs());
        return commandLine;
    }
}
