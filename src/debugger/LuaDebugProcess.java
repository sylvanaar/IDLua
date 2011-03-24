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

import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.sylvanaar.idea.Lua.run.LuaCommandLineState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/19/11
 * Time: 7:40 PM
 */
public class LuaDebugProcess extends XDebugProcess {
    private static final Logger log = Logger.getInstance("#Lua.LuaDebugProcess");
    LuaDebuggerController controller;
    LuaCommandLineState state;
    private Future<?> controllerFuture;
    private boolean myClosing;

    /**
     * @param session pass <code>session</code> parameter of {@link com.intellij.xdebugger
     *                .XDebugProcessStarter#start} method to this constructor
     * @param luaCommandLineState
     */
    protected LuaDebugProcess(@NotNull XDebugSession session, LuaCommandLineState luaCommandLineState) {
        super(session);

        controller = new LuaDebuggerController();

        state = luaCommandLineState;
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return new LuaDebuggerEditorsProvider();
    }

    @Override
    public void startStepOver() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startStepInto() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startStepOut() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        if (controllerFuture != null && !controllerFuture.isDone()) controllerFuture.cancel(true);

        controllerFuture = null;
    }

    @Override
    public void resume() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void sessionInitialized() {
        super.sessionInitialized();
        ProgressManager.getInstance().run(
                new Task.Backgroundable(null, "Connecting to debugger", false) {

                    public void run(@NotNull ProgressIndicator indicator) {
                        indicator.setText("Connecting to debugger...");

                        try {
                            controller.waitForConnect();

                            getSession().rebuildViews();

//                       registerBreakpoints();
                            //(new RunCommand(myDebugger)).execute();
                        } catch (final Exception e) {

                            //myProcessHandler.destroyProcess();

                            if (!myClosing) SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    Messages.showErrorDialog((new StringBuilder()).append(
                                            "Unable to establish connection with debugger:\n").append(
                                            e.getMessage()).toString(), "Connecting to debugger");
                                }
                            });
                        }
                    }
                });
    }

}
