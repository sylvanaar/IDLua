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

package com.sylvanaar.idea.Lua.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.text.StringUtil;

/**
 * This code bases on the intellij-batch plugin by wibotwi.
 *
 * @author wibotwi, jansorg
 */
public class LuaCommandLineState extends CommandLineState {
    private final LuaRunConfiguration runConfiguration;

    public LuaCommandLineState(LuaRunConfiguration runConfiguration, ExecutionEnvironment env) {
        super(env);
        this.runConfiguration = runConfiguration;
    }

    @Override
    protected OSProcessHandler startProcess() throws ExecutionException {
        GeneralCommandLine commandLine = generateCommandLine();

        OSProcessHandler osProcessHandler = new OSProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString());
        osProcessHandler.putUserData(OSProcessHandler.SILENTLY_DESTROY_ON_CLOSE, Boolean.TRUE);
        ProcessTerminatedListener.attach(osProcessHandler, getRunConfiguration().getProject());

        return osProcessHandler;
    }

    private GeneralCommandLine generateCommandLine() {
        GeneralCommandLine commandLine = new GeneralCommandLine();

        if (!StringUtil.isEmptyOrSpaces(getRunConfiguration().getInterpreterPath()))
            commandLine.setExePath(getRunConfiguration().getInterpreterPath());
        


        commandLine.getParametersList().addParametersString(getRunConfiguration().getInterpreterOptions());

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

    protected LuaRunConfiguration getRunConfiguration() {
        return runConfiguration;
    }
}