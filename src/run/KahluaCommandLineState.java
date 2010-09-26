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
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.KahLuaInterpreterWindowFactory;
import org.jetbrains.annotations.NotNull;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.LuaClosure;

import java.io.IOException;
import java.io.InputStream;

/**
* Created by IntelliJ IDEA.
* User: Jon S Akhtar
* Date: Aug 28, 2010
* Time: 6:35:19 PM
*/


public class KahluaCommandLineState extends LuaCommandLineState {
    private static final Logger log = Logger.getInstance("#Lua.KahluaCommandLineState");
    final KahluaConverterManager manager = new KahluaConverterManager();
    final LuaCaller caller = new LuaCaller(manager);


    public KahluaCommandLineState(LuaRunConfiguration runConfiguration, ExecutionEnvironment env) {
        super(runConfiguration, env);
    }

    public ExecutionResult execute(@NotNull final Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
        log.info("execute " + executor.getActionName());


        final ProcessHandler processHandler = startProcess();
        final TextConsoleBuilder builder = getConsoleBuilder();
        final ConsoleView console = builder != null ? builder.getConsole() : null;
//        if (console != null) {
//          console.attachToProcess(processHandler);
//        }

        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(getRunConfiguration().getScriptName());

        final String text;
        if (file != null) {
            text = FileDocumentManager.getInstance().getDocument(file).getText();
        } else text = "";

        if (KahLuaInterpreterWindowFactory.INSTANCE != null) {
            KahLuaInterpreterWindowFactory.WINDOW.activate(KahLuaInterpreterWindowFactory.INSTANCE.getRunnableExecution(text), true);

        }

//        try {
//            File script = new File( getRunConfiguration().getScriptName());
//
//            assert(script.exists());
//
//            final Platform platform = new J2SEPlatform();
//            final KahluaTable env = platform.newEnvironment();
//
//            KahluaConverterManager manager = new KahluaConverterManager();
//            KahluaNumberConverter.install(manager);
//            KahluaEnumConverter.install(manager);
//            new KahluaTableConverter(platform).install(manager);
//
//            KahluaTable staticBase = platform.newTable();
//            env.rawset("Java", staticBase);
//            KahluaThread thread = new KahluaThread(platform, env);
//
//
//            LuaClosure luaClosure = null;
//            try {
//                FileInputStream is = new FileInputStream(script.getAbsolutePath());
//                if (is.available()>0) {
//                    try {
//                       luaClosure = compileScript(is, script.getName(), thread.getEnvironment());
//                    } catch (KahluaException err) {
//                       // makeErrorAnnotation(file, holder, err.getMessage());
//                    }
//                }
//
//                is.close();
//            } catch (UnsupportedEncodingException e) {
//               err("Encoding Error", e);
//            }
//
//
//            LuaReturn result = caller.protectedCall(thread, luaClosure);
//            if (result.isSuccess()) {
//                for (Object o : result) {
//                     msg(KahluaUtil.tostring(o, thread)+"\n");
//                }
//            } else {
//                err(result.getErrorString()+"\n");
//                err(result.getLuaStackTrace()+"\n");
//               // result.getJavaException().printStackTrace(System.err);
//            }
//        } catch (IOException e) {
//           err(e.toString());
//        } catch (RuntimeException e) {
//           err(e.getMessage()+"\n");
//        }

        return new KahluaExecutionResult(console, createActions(console, processHandler, executor));
    }

    private void msg(String msg) {
      //  log.info(msg);

        KahLuaInterpreterWindowFactory.INSTANCE.getTerminal().appendOutput(msg);



//        getConsoleBuilder().getConsole().print(msg,
//                             ConsoleViewContentType.NORMAL_OUTPUT);
    }
    private void err(String msg) { err(msg, null); }
    private void err(String msg, Throwable t) {
       // log.error(msg, t);
        KahLuaInterpreterWindowFactory.INSTANCE.getTerminal().appendError(msg);

//        getConsoleBuilder().getConsole().print(msg,
//                             ConsoleViewContentType.ERROR_OUTPUT);
    }
    
    @Override
    protected OSProcessHandler startProcess() throws ExecutionException {
        log.info("startProcess");

        KahluaProcessHandler ph = new KahluaProcessHandler(null, null);
        return null;
    }

    private LuaClosure compileScript(InputStream script, String chunkname, KahluaTable environment) throws IOException {
        LuaClosure luaClosure;
//        try {
//            luaClosure = LuaCompiler.loadis("return  " + script, chunkname, environment);
//        } catch (KahluaException e) {
//            // Ignore it and try without "return "
            luaClosure = LuaCompiler.loadis(script, chunkname, environment);
        return luaClosure;
    }

    @Override
    public RunnerSettings getRunnerSettings() {
        return null;
    }

    @Override
    public ConfigurationPerRunnerSettings getConfigurationSettings() {
        return null;
    }
}
