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

package com.sylvanaar.idea.Lua.console;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.console.LanguageConsoleViewImpl;
import com.intellij.execution.process.CommandLineArgumentsProvider;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory;
import com.intellij.execution.runners.ConsoleExecuteActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.ArrayUtil;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
import com.sylvanaar.idea.Lua.util.LuaSystemUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/20/11
 * Time: 4:51 PM
 */
public class LuaConsoleRunner extends AbstractConsoleRunnerWithHistory {
    public LuaConsoleRunner(@NotNull Project project, @NotNull String consoleTitle,
                            @NotNull CommandLineArgumentsProvider provider,
                            @org.jetbrains.annotations.Nullable String workingDir) {
        super(project, consoleTitle, provider, workingDir);
    }

    @Override
    protected LanguageConsoleViewImpl createConsoleView() {
        return new LuaLanguageConsoleView(getProject(), getConsoleTitle());
    }

    @Override
    protected Process createProcess(CommandLineArgumentsProvider provider) throws ExecutionException {
        return createLuaProcess(getWorkingDir(), provider);
    }

    @Override
    protected OSProcessHandler createProcessHandler(Process process, String commandLine) {
        return new LuaConsoleProcessHandler(process, getConsoleView().getConsole(), commandLine,
                CharsetToolkit.UTF8_CHARSET);
    }

    @NotNull
    @Override
    protected ConsoleExecuteActionHandler createConsoleExecuteActionHandler() {
        return new LuaConsoleExecuteActionHandler(getProcessHandler(), false);
    }


    public static void run(Project project, Sdk sdk, String consoleTitle, String projectRoot,
                           String statements2execute[]) {
        LuaConsoleRunner runner = new LuaConsoleRunner(project, consoleTitle, new MyCommandLineArgumentsProvider(sdk), projectRoot);

        try {
            runner.initAndRun();
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static Process createLuaProcess(String workingDir,
                                           CommandLineArgumentsProvider provider) throws ExecutionException {
        String[] command = provider.getArguments();
        assert command != null;

        String arguments[];
        if (command.length > 1) {
            arguments = new String[command.length - 1];
            System.arraycopy(command, 1, arguments, 0, command.length - 1);
        } else {
            arguments = ArrayUtil.EMPTY_STRING_ARRAY;
        }
        GeneralCommandLine cmdLine = createAndSetupCmdLine(null, workingDir, provider.getAdditionalEnvs(), true,
                command[0], arguments);
        return cmdLine.createProcess();
    }

    public static GeneralCommandLine createAndSetupCmdLine(String additionalLoadPath, String workingDir,
                                                           Map userDefinedEnvs, boolean passParentEnvs,
                                                           String executablePath, String arguments[]) {
        assert executablePath != null;
        assert arguments != null;

        GeneralCommandLine cmdLine = new GeneralCommandLine();
        List fixedArguments = new ArrayList();
        Collections.addAll(fixedArguments, arguments);
        cmdLine.setExePath(FileUtil.toSystemDependentName(executablePath));
        if (workingDir != null) cmdLine.setWorkDirectory(FileUtil.toSystemDependentName(workingDir));
        cmdLine.addParameters(fixedArguments);
        Map customEnvVariables;
        if (userDefinedEnvs == null) customEnvVariables = new HashMap();
        else customEnvVariables = new HashMap(userDefinedEnvs);
        cmdLine.setPassParentEnvs(passParentEnvs);
        EnvironmentVariablesComponent.inlineParentOccurrences(customEnvVariables);
        Map envParams = new HashMap();
        if (passParentEnvs) envParams.putAll(System.getenv());
        envParams.putAll(customEnvVariables);
        String PATH_KEY = LuaSystemUtil.getPATHenvVariableName();
        if(!StringUtil.isEmpty(additionalLoadPath))
        {
            String path = (String)envParams.get(PATH_KEY);
            envParams.put(PATH_KEY, LuaSystemUtil.appendToPATHenvVariable(path, additionalLoadPath));
        }
        cmdLine.setEnvParams(envParams);
        return cmdLine;
    }

    private static class MyCommandLineArgumentsProvider extends CommandLineArgumentsProvider {
        private Sdk mySdk;

        public MyCommandLineArgumentsProvider(Sdk sdk) {
            mySdk = sdk;
        }

        @Override
        public String[] getArguments() {
            return new String[]{LuaSdkType.getTopLevelExecutable(mySdk.getHomePath()).getAbsolutePath(), "-i"};
        }

        @Override
        public boolean passParentEnvs() { return false; }

        @Override
        @Nullable
        public Map<String, String> getAdditionalEnvs() { return Collections.emptyMap(); }
    }
}
