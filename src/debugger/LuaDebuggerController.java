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

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XSuspendContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/22/11
 * Time: 6:31 PM
 */
public class LuaDebuggerController {
    private static final Logger log = Logger.getInstance("Lua.LuaDebuggerController");
    ServerSocket serverSocket;
    Socket clientSocket;
    int serverPort = 8171;

    SocketReader reader = null;
    OutputStream outputStream = null;

    static final String RUN = "RUN\n";
    static final String STEP = "STEP\n";
    static final String STEP_OVER = "OVER\n";

    private boolean readerCanRun = true;

    Pattern AT_BREAKPOINT;
    private XDebugSession session;
    private ConsoleView console;
    private boolean ready;

    public XDebugSession getSession() { return session; }

    Map<XBreakpoint, LuaPosition> myBreakpoints2Pos = new HashMap<XBreakpoint, LuaPosition>();
    Map<LuaPosition, XBreakpoint> myPos2Breakpoints = new HashMap<LuaPosition, XBreakpoint>();

    Project myProject = null;

    LuaDebuggerController(XDebugSession session) {
        myProject = session.getProject();
        this.session = session;
        this.session.setPauseActionSupported(false);
        AT_BREAKPOINT = Pattern.compile("^202 Paused\\s+(\\S+)\\s+(\\d+)", Pattern.MULTILINE);

        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                log.debug("Starting Debug Controller");
                try {
                    serverSocket = new ServerSocket(serverPort);
                    log.debug("Accepting Connections");
                    clientSocket = serverSocket.accept();
                    log.debug("Client Connected " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
    }

    public void printToConsole(String text, ConsoleViewContentType contentType)
    {
        assert console != null;
        
        console.print(text + '\n', contentType);
    }
    
    public void waitForConnect() throws IOException {

        int count = 0;
        while (clientSocket == null) {
            try {
                Thread.sleep(200);
                if (++count > 20)
                    throw new RuntimeException("timeout");
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        printToConsole("Debugger connected at " + clientSocket.getInetAddress(), ConsoleViewContentType.SYSTEM_OUTPUT);

        reader = new SocketReader();
        reader.start();

        outputStream = clientSocket.getOutputStream();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ready = true;
    }

    public void terminate() {
        log.debug("terminate");
        readerCanRun = false;

        try {
            if (serverSocket!=null)
                serverSocket.close();
            if (clientSocket != null)
                clientSocket.close();
            ready = false;
        } catch (IOException e) {
            e.printStackTrace();  
        }
    }

    public void stepInto() {
        try {
            log.debug("stepInto");

            outputStream.write(STEP.getBytes("UTF8"));
            ready = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stepOver() {
        try {
            log.debug("stepOver");

            outputStream.write(STEP_OVER.getBytes("UTF8"));
            ready = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        try {
            log.debug("resume");
            outputStream.write(RUN.getBytes("UTF8"));
            ready = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setConsole(ConsoleView console) {
        this.console = console;
    }

    public void addBreakPoint(XBreakpoint breakpoint) {
        try {
            LuaPosition pos = LuaPositionConverter.createRemotePosition(breakpoint.getSourcePosition());
            
            String msg = String.format("SETB %s %d\n", pos.getPath(), pos.getLine());

            log.debug(msg);
            
            outputStream.write(msg.getBytes("UTF8"));
            
            myBreakpoints2Pos.put(breakpoint, pos);
            myPos2Breakpoints.put(pos, breakpoint);
            
        //    ready = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeBreakPoint(XBreakpoint breakpoint) {
        try {
            LuaPosition pos = LuaPositionConverter.createRemotePosition(breakpoint.getSourcePosition());
            String msg = String.format("DELB %s %d\n", pos.getPath(), pos.getLine());

            log.debug(msg);

            if (outputStream != null)
                outputStream.write(msg.getBytes("UTF8"));

            myBreakpoints2Pos.remove(breakpoint);
            myPos2Breakpoints.remove(pos);

           // ready = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isReady() {
        return ready;
    }

    Queue<CodeExecutionRequest> myPendingCallbacks = new ArrayBlockingQueue<CodeExecutionRequest>(5);
    
    public void execute(CodeExecutionRequest codeExecutionRequest) {
        myPendingCallbacks.add(codeExecutionRequest);

        executePendingRequest();
    }

    private void executePendingRequest() {
        CodeExecutionRequest codeExecutionRequest = myPendingCallbacks.peek();

        if (codeExecutionRequest == null || codeExecutionRequest.isInProgress())
            return;
        codeExecutionRequest.setInProgress(true);
        
        try {

            String msg = String.format("EXEC %s\n", codeExecutionRequest.getCode());

            log.debug(msg);

            outputStream.write(msg.getBytes("UTF8"));

            ready = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


    class SocketReader extends Thread {
        public SocketReader() {
            super("DebuggerSocketReader");
        }

        @Override
        public void run() {
            log.debug("Read thread started");

            byte[] buffer = new byte[1000];
            InputStream input = null;
            try {
                input = clientSocket.getInputStream();


            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            while (readerCanRun) {
                try {
                    while (input.available() > 0) {
                        assert input != null;
                        int count = 0;
                        if ((count = input.read(buffer)) > 0)
                            processResponse(new String(buffer, 0, count, CharsetToolkit.UTF8));
                        else
                            log.debug("No data to read");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    String cleanupFileName(String name) {
        if (name.indexOf(':') != name.lastIndexOf(':')) {
            int last = name.lastIndexOf(':');

            return name.substring(last-1);
        }

        return name;
    }

    XSuspendContext EMPTY_CTX = new XSuspendContext() {};

    private void processResponse(String messages)  {
        log.debug("Response: <"+messages+">");

        String[] lines = messages.split("\n");

        for (int i = 0, linesLength = lines.length; i < linesLength; i++) {
            String message = lines[i];
            log.debug("Processing: " + message);

            if (message.startsWith("200")) {
                processOK(message.substring(Math.min(7, message.length())));
                continue;
            }

            Matcher m = AT_BREAKPOINT.matcher(message);

            if (m.matches()) {
                String file = m.group(1);
                String line = m.group(2);


                String stack = lines[i+1];

                log.debug(String.format("break at <%s> line <%s> stack <%s>", file, line, stack));

                LuaPosition position = new LuaPosition(file, Integer.parseInt(line));

                XBreakpoint bp = myPos2Breakpoints.get(position);

                ready = true;

                LuaSuspendContext ctx;

                if (bp != null)
                   ctx = new LuaSuspendContext(myProject, this, bp, stack);
                else
                   ctx = new LuaSuspendContext(myProject, this, LuaPositionConverter.createLocalPosition(position), stack);



                if (bp != null) session.breakpointReached(bp, null, ctx);
                else { session.positionReached(ctx); }


                // This makes the watch expressions update correctly at the start of a suspend context
                // This is a hack.
//                DebuggerUIUtil.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(1500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                        }
//                        XDebugSessionImpl xDebugSession = (XDebugSessionImpl) session;
//                        xDebugSession.activateSession();
//                    }
//                });

                continue;
            }
        }
    }

    private void processOK(String message) {
        ready = true;

        CodeExecutionRequest executionRequest = myPendingCallbacks.poll();
        if (executionRequest != null && message.length() > 0) {
            log.debug(String.format("Processing OK Payload: <%s>", message));

            final int typeEnd = message.indexOf(' ');
            String type = message.substring(0, typeEnd);
            message = message.substring(typeEnd+1);

            LuaDebugValue value = new LuaDebugValue(type, message);

            executionRequest.getCallback().evaluated(value);

             ApplicationManager.getApplication().invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     executePendingRequest();
                 }
             });
        }
    }

    static class CodeExecutionRequest {
        private final String                                 code;
        private final XDebuggerEvaluator.XEvaluationCallback callback;
        private boolean inProgress = false;

        CodeExecutionRequest(String code, XDebuggerEvaluator.XEvaluationCallback callback) {
            this.code = code;
            this.callback = callback;
        }

        public String getCode() {
            return code;
        }

        public XDebuggerEvaluator.XEvaluationCallback getCallback() {
            return callback;
        }

        public boolean isInProgress() {
            return inProgress;
        }

        public void setInProgress(boolean inProgress) {
            this.inProgress = inProgress;
        }
    }
}
