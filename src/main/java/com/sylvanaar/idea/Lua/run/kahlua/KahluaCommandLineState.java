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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.kahlua.KahLuaInterpreterWindowFactory;
import com.sylvanaar.idea.Lua.run.LuaRunConfiguration;
import com.sylvanaar.idea.Lua.run.LuaRunConfigurationParams;
import com.sylvanaar.idea.Lua.run.lua.LuaCommandLineState;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Aug 28, 2010
 * Time: 6:35:19 PM
 */


public class KahluaCommandLineState extends LuaCommandLineState {
    private static final Logger log = Logger.getInstance("Lua.KahluaCommandLineState");

    public KahluaCommandLineState(LuaRunConfiguration runConfiguration, ExecutionEnvironment env) {
        super(runConfiguration, env);
    }

    public ExecutionResult execute(@NotNull final Executor executor,
                                   @NotNull ProgramRunner runner) throws ExecutionException {
        log.info("execute " + executor.getActionName());

        final ProcessHandler processHandler = startProcess();
        final TextConsoleBuilder builder = getConsoleBuilder();
        final ConsoleView console = builder != null ? builder.getConsole() : null;
        if (console != null) {
            console.attachToProcess(processHandler);
        }

        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(
                ((LuaRunConfigurationParams) getRunConfiguration()).getScriptName());

        final String text;
        if (file != null) {
            final Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document != null) {
                text = document.getText();
            } else
                text = "";
        } else
            text = "";

        if (KahLuaInterpreterWindowFactory.INSTANCE != null) {
            KahLuaInterpreterWindowFactory.WINDOW
                                          .activate(KahLuaInterpreterWindowFactory.INSTANCE.getRunnableExecution(text),
                                                  true);
        }

        return new KahluaExecutionResult(console, createActions(console, processHandler, executor));
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        log.info("startProcess");
        return new KahluaProcessHandler();
    }

    @Override
    public RunnerSettings getRunnerSettings() {
        return null;
    }
}
