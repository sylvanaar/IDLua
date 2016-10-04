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

package com.sylvanaar.idea.Lua.run.lua;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
import com.sylvanaar.idea.Lua.options.LuaApplicationSettings;
import com.sylvanaar.idea.Lua.options.LuaInterpreter;
import com.sylvanaar.idea.Lua.options.LuaInterpreterFamily;
import com.sylvanaar.idea.Lua.options.LuaInterpreterFinder;
import com.sylvanaar.idea.Lua.run.LuaRunConfiguration;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
import org.jetbrains.annotations.NotNull;

import static com.sylvanaar.idea.Lua.sdk.LuaSdkType.getTopLevelExecutable;

public class LuaCommandLineState extends CommandLineState {
    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    private final RunConfiguration     runConfiguration;
    private final ExecutionEnvironment executionEnvironment;

    public LuaCommandLineState(RunConfiguration runConfiguration, ExecutionEnvironment env) {
        super(env);
        this.runConfiguration = runConfiguration;
        this.executionEnvironment = env;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        GeneralCommandLine commandLine = generateCommandLine();

        OSProcessHandler osProcessHandler =
                new LuaProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString());
        ProcessTerminatedListener.attach(osProcessHandler, runConfiguration.getProject());

        return osProcessHandler;
    }

    private void fillInterpreterCommandLine(GeneralCommandLine commandLine) {
        // Check the default from LuaApplicationSettings
        LuaApplicationSettings settings = LuaApplicationSettings.getInstance();
        LuaInterpreter defaultInterpreter = settings.getDefaultInterpreter();
        if (defaultInterpreter == null)
            return;

        LuaInterpreterFamily family = defaultInterpreter.getFamily();
        if (family == LuaInterpreterFamily.UNKNOWN_INTERPRETER) {
            LuaInterpreterFinder.INSTANCE.describe(defaultInterpreter);
            family = defaultInterpreter.getFamily();
        }

        if (family == LuaInterpreterFamily.INVALID_INTERPRETER) {
            // TODO: Error message
            return;
        }

        switch (family.getBinaryType()) {
            case SystemBinary:
                commandLine.setExePath(defaultInterpreter.path);
                break;

            case JavaJar:
                commandLine.setExePath("java");
                commandLine.getParametersList().addAll(
                        "-cp",
                        defaultInterpreter.path,
                        "lua"
                );
                break;
        }
    }

    protected GeneralCommandLine generateCommandLine() {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        final LuaRunConfiguration cfg = (LuaRunConfiguration) runConfiguration;

        if (cfg.isOverrideSDKInterpreter()) {
            if (!StringUtil.isEmptyOrSpaces(cfg.getInterpreterPath()))
                commandLine.setExePath(cfg.getInterpreterPath());
        } else {
            final Sdk sdk = cfg.getSdk();

            if (sdk == null) {
                fillInterpreterCommandLine(commandLine);
            } else if (sdk.getSdkType() instanceof LuaSdkType) {
                String sdkHomePath = StringUtil.notNullize(sdk.getHomePath());
                String sdkInterpreter = getTopLevelExecutable(sdkHomePath).getAbsolutePath();
                commandLine.setExePath(sdkInterpreter);
            }
        }

        commandLine.getEnvironment().putAll(cfg.getEnvs());
        commandLine.setPassParentEnvironment(cfg.isPassParentEnvs());

        return configureCommandLine(commandLine);
    }

    protected GeneralCommandLine configureCommandLine(GeneralCommandLine commandLine) {
        final LuaRunConfiguration configuration = (LuaRunConfiguration) runConfiguration;
        commandLine.getParametersList().addParametersString(configuration.getInterpreterOptions());

        if (!StringUtil.isEmptyOrSpaces(configuration.getWorkingDirectory())) {
            commandLine.setWorkDirectory(configuration.getWorkingDirectory());
        }

        if (!StringUtil.isEmptyOrSpaces(configuration.getScriptName())) {
            commandLine.addParameter(configuration.getScriptName());
        }

        commandLine.getParametersList().addParametersString(configuration.getScriptParameters());

        return commandLine;
    }


    protected RunConfiguration getRunConfiguration() {
        return runConfiguration;
    }
}