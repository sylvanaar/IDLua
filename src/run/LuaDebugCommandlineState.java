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

import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.run.lua.LuaCommandLineState;
import com.sylvanaar.idea.Lua.sdk.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 5/26/11
 * Time: 1:58 AM
 */
public class LuaDebugCommandlineState extends LuaCommandLineState {
    public LuaDebugCommandlineState(RunConfiguration runConfiguration, ExecutionEnvironment env) {
        super(runConfiguration, env);
    }

    @Override
    protected GeneralCommandLine configureCommandLine(GeneralCommandLine commandLine) {
        final LuaRunConfigurationParams configuration = (LuaRunConfigurationParams) getRunConfiguration();

        // Verify that we have properly defined the debug module
        final VirtualFile debugModuleLocation = StdLibrary.getDebugModuleLocation();
        if (debugModuleLocation == null)
            return commandLine;

        // Collect the parameters for the lua interpreter
        final ParametersList params = commandLine.getParametersList();

        // Load the debugger module before starting the user script
        final String remDebugPath = debugModuleLocation.getPath();
        params.addParametersString("-e");
        params.add("package.path=[[" + remDebugPath + "/?.lua;]]  ..  package.path");
        params.addParametersString("-l remdebug");

        // Add the user-defined interpreter options
        final CommonLuaRunConfigurationParams commonParams = configuration.getCommonParams();
        final String interpreterOptions = commonParams.getInterpreterOptions();
        params.addParametersString(interpreterOptions);

        // Add the user script
        final String scriptName = configuration.getScriptName();
        if (!StringUtil.isEmptyOrSpaces(scriptName))
            params.addParametersString(scriptName);

        // Add the user script parameters
        final String scriptParameters = configuration.getScriptParameters();
        if (!StringUtil.isEmptyOrSpaces(scriptParameters))
            params.addParametersString(scriptParameters);

        // Set the working directory
        final String workingDirectory = commonParams.getWorkingDirectory();
        if (!StringUtil.isEmptyOrSpaces(workingDirectory))
            commandLine.setWorkDirectory(workingDirectory);

        return commandLine;
    }
}
