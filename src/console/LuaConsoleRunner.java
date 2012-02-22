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

import com.intellij.execution.*;
import com.intellij.execution.configuration.*;
import com.intellij.execution.configurations.*;
import com.intellij.execution.console.*;
import com.intellij.execution.process.*;
import com.intellij.execution.runners.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.util.io.*;
import com.intellij.openapi.util.text.*;
import com.intellij.openapi.vfs.*;
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.sdk.*;
import com.sylvanaar.idea.Lua.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/20/11
 * Time: 4:51 PM
 */
public class LuaConsoleRunner extends AbstractConsoleRunnerWithHistory<LuaLanguageConsole.View> {
    public LuaConsoleRunner(@NotNull Project project, @NotNull String consoleTitle,
//                            @NotNull CommandLineArgumentsProvider provider,
                            @org.jetbrains.annotations.Nullable String workingDir) {
        super(project, consoleTitle, /*provider,*/ workingDir);
    }

    LuaLanguageConsole.View view;
    @Override
    protected LuaLanguageConsole.View createConsoleView() {
        if (view == null)
            view = new LuaLanguageConsole.View(getProject(), getConsoleTitle());

        return view;
    }

    @Override
    public LuaLanguageConsole.View getConsoleView() {
        return createConsoleView();
    }

    //    @Override
//    protected Process createProcess(CommandLineArgumentsProvider provider) throws ExecutionException {
//        return createLuaProcess(getWorkingDir(), provider);
//    }

    OSProcessHandler  myProcessHandler;
    protected OSProcessHandler createProcessHandler(Process process, String commandLine) {
        if (myProcessHandler == null)
            myProcessHandler = new LuaConsoleProcessHandler(process, getConsoleView().getConsole(), commandLine,
                    CharsetToolkit.UTF8_CHARSET);
        return myProcessHandler;
    }

    @Override
    protected OSProcessHandler createProcess() throws ExecutionException {
        return myProcessHandler;
    }

    @NotNull
    @Override
    protected ConsoleExecuteActionHandler createConsoleExecuteActionHandler() {
        return new LuaLanguageConsole.ActionHandler(getProcessHandler(), false);
    }


    // This code is used to invoke the console
    public static void run(Project project, Sdk sdk, String consoleTitle, String workingDir) {
        try {
            LuaConsoleRunner runner = new LuaConsoleRunner(project, consoleTitle, workingDir);

            // TODO - figure out the best way to pass the process to to the process handler
            final GeneralCommandLine cmd = createLuaCommandLine(workingDir, new MyCommandLineArgumentsProvider(sdk));
            runner.createProcessHandler(cmd.createProcess(), cmd.getCommandLineString());

            runner.initAndRun();  // Calls createProcess
            ConsoleHistoryController chc =
                    new ConsoleHistoryController(consoleTitle, null, runner.getConsoleView().getConsole(),
                            runner.getConsoleExecuteActionHandler().getConsoleHistoryModel());

            chc.install();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static GeneralCommandLine createLuaCommandLine(String workingDir,
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
        return cmdLine;
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
            return new String[]{LuaSdkType.getTopLevelExecutable(mySdk.getHomePath()).getAbsolutePath(), "-i", "-e",
                    "io.stdout:setvbuf([[no]])"};
        }

        @Override
        public boolean passParentEnvs() { return false; }

        @Override
        @Nullable
        public Map<String, String> getAdditionalEnvs() { return Collections.emptyMap(); }
    }
}
