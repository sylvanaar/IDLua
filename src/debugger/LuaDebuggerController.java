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

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.sylvanaar.idea.Lua.run.LuaRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for interacting with the remote debugger client.
 *
 * Listens for and accepts connections on the standard Lua
 * debug port (8171).  After accepting a connection, sends a
 * list of breakpoints and begins execution.
 *
 * Remote client is responsible for stopping at any set breakpoints,
 * and returning a call stack upon doing so.
 */
public class LuaDebuggerController {
    private static final Logger log = Logger.getInstance("Lua.LuaDebuggerController");
    ServerSocket serverSocket;
    Socket clientSocket;
    int serverPort = 8171;

    SocketReader reader = null;
    OutputStream outputStream = null;

    static final String RUN = "RUN";
    static final String STEP = "STEP";
    static final String STEP_OVER = "OVER";
    static final String STEP_OUT = "OUT";
    static final String BASEDIR = "BASEDIR";

    private Queue<DebugRequest> pendingRequests = new ArrayBlockingQueue<DebugRequest>(16);
    private XDebugSession session;
    private ConsoleView console;
    private boolean ready;

    private Pattern RESPONSE_BP = Pattern.compile("^202 Paused\\s+(\\S+)\\s+(\\d+)", Pattern.MULTILINE);
    private Pattern RESPONSE_WP = Pattern.compile("^203 Paused\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)", Pattern.MULTILINE);
    private Pattern RESPONSE_ERR = Pattern.compile("^401 (.+)\\s+(\\d+)", Pattern.MULTILINE);

    Map<XBreakpoint, LuaPosition> myBreakpoints2Pos = new HashMap<XBreakpoint, LuaPosition>();
    Map<LuaPosition, XBreakpoint> myPos2Breakpoints = new HashMap<LuaPosition, XBreakpoint>();

    Project myProject = null;

    private String baseDir = null;

    LuaDebuggerController(XDebugSession session) {
        myProject = session.getProject();

        this.session = session;
        this.session.setPauseActionSupported(false);

        baseDir = getWorkingDir();

        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                log.info("Starting Debug Controller");
                try {
                    serverSocket = new ServerSocket(serverPort);
                    log.info("Accepting Connections");
                    clientSocket = serverSocket.accept();
                    log.info("Client Connected " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    log.info("Failed to accept client connection.");
                }
            }
        });
    }

    private String getWorkingDir() {

        String workingDir = null;
        RunProfile profile = this.session.getRunProfile();

        if (profile != null && profile instanceof LuaRunConfiguration) {
            workingDir = ((LuaRunConfiguration) profile).getWorkingDirectory();
        }
        if(StringUtil.isEmpty(workingDir)) {
            workingDir = myProject.getBaseDir().getPath();
        }
        if(!workingDir.endsWith(File.separator)) workingDir += File.separator;

        return workingDir;
    }

    public void printToConsole(String text, ConsoleViewContentType contentType) {
        assert console != null;
        
        console.print(text + '\n', contentType);
    }
    
    public void waitForConnect() throws IOException {

        int count = 0;
        while (clientSocket == null) {
            try {
                Thread.sleep(100);
                if (++count > 50)
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

        setBaseDir();
    }

    public void terminate() {
        log.debug("terminate");

        if (reader != null)
            reader.cancel();

        try {
            if (serverSocket!=null)
                serverSocket.close();
            if (clientSocket != null)
                clientSocket.close();
            ready = false;
        } catch (IOException ignored) {}
    }

    public void setConsole(ConsoleView console) {
        this.console = console;
    }

    public boolean isReady() {
        return ready;
    }


    //////////////////////////////
    // Remote Requests

    public void stepInto() {
        queueRequest(new SimpleCommandRequest(STEP));
    }

    public void stepOver() {
        queueRequest(new SimpleCommandRequest(STEP_OVER));
    }

    public void stepOut() {
        queueRequest(new SimpleCommandRequest(STEP_OUT));
    }

    public void resume() {
        queueRequest(new SimpleCommandRequest(RUN));
    }

    public void setBaseDir() {
        queueRequest(new SimpleCommandRequest(String.format("%s %s", BASEDIR, baseDir)));
    }

    public void addBreakPoint(XBreakpoint breakpoint) {
        LuaPosition pos = LuaPositionConverter.createRemotePosition(breakpoint.getSourcePosition());
        String msg = String.format("SETB %s %d", pos.getPath(), pos.getLine());
        queueRequest(new SimpleCommandRequest(msg));

        myBreakpoints2Pos.put(breakpoint, pos);
        myPos2Breakpoints.put(pos, breakpoint);
    }

    public void removeBreakPoint(XBreakpoint breakpoint) {
        LuaPosition pos = LuaPositionConverter.createRemotePosition(breakpoint.getSourcePosition());
        String msg = String.format("DELB %s %d", pos.getPath(), pos.getLine());
        queueRequest(new SimpleCommandRequest(msg));

        myBreakpoints2Pos.remove(breakpoint);
        myPos2Breakpoints.remove(pos);
    }

    public Promise<LuaDebugValue> execute(String statement) {
        AsyncPromise<LuaDebugValue> result = new AsyncPromise<LuaDebugValue>();
        CodeExecutionRequest codeExecutionRequest = new CodeExecutionRequest(result, statement);
        queueRequest(codeExecutionRequest);
        return result;
    }

    public Promise<List<LuaDebugVariable>> variables(int frameIndex) {
        AsyncPromise<List<LuaDebugVariable>> result = new AsyncPromise<List<LuaDebugVariable>>();
        if (frameIndex == 0) {
            result.setError("Non-existent stack frame");
        } else {
            DebugRequest stackRequest = new StackRequest(result, frameIndex);
            queueRequest(stackRequest);
        }
        return result;
    }

    private void queueRequest(DebugRequest request) {
        pendingRequests.add(request);
        startPendingRequest();
    }

    private void startPendingRequest() {
        DebugRequest debugRequest = pendingRequests.peek();

        if (debugRequest == null || debugRequest.isInProgress())
            return;

        debugRequest.setInProgress(true);

        String msg = debugRequest.getCommand();

        try {
            // Send the request command
            log.debug(msg);
            outputStream.write(msg.getBytes("UTF8"));
            ready = false;
        } catch (IOException e) {
            log.info(String.format("Failed to send command: %s", msg), e);
        }
    }

    class SocketReader extends Thread {
        private boolean cancelled = false;
        private BufferedReader bufferedReader;

        public SocketReader() {
            super("DebuggerSocketReader");
        }

        public void cancel() {
            cancelled = true;
        }

        @NotNull
        private BufferedReader getReader() throws IOException {
            if (bufferedReader != null)
                return bufferedReader;

            InputStream input;
            try {
                input = clientSocket.getInputStream();
            } catch (IOException e) {
                log.info("Failed to obtain input stream from client socket", e);
                cancel();
                throw e;
            }

            InputStreamReader streamReader;
            try {
                streamReader = new InputStreamReader(input, CharsetToolkit.UTF8);
            } catch (UnsupportedEncodingException e) {
                log.info("InputStreamReader does not support UTF-8", e);
                cancel();
                throw new IOException(e);
            }

            bufferedReader = new BufferedReader(streamReader);
            return bufferedReader;
        }

        public String readLine() throws IOException {
            return getReader().readLine();
        }

        public String readBytes(int count) throws IOException {
            char buffer[] = new char[count];
            int didRead = 0;
            while (didRead < count)
                didRead += getReader().read(buffer, didRead, count - didRead);
            return new String(buffer);
        }

        @Override
        public void run() {
            log.debug("Read thread started");

            while (!cancelled) {
                try {
                    // Read one line at a time
                    String message = readLine();
                    if (message == null)
                        break;
                    if (message.length() > 0)
                        processMessage(message);
                    else
                        log.debug("No data to read");
                } catch (IOException e) {
                    log.info("Failed to read data from client socket input stream", e);
                    break;
                }
            }
        }
    }


    private Integer getCode(String s) {
        final int len  = s.length( );
        char ch = s.charAt( 0 );

        if (ch < '0' || ch > '9')
            return null;

        int num  = ch - '0';

        // Build the number.
        int i = 1;
        while ( i < len ) {
            ch = s.charAt(i++);
            if (ch == ' ')
                break;
            num = num * 10 + ch - '0';
        }

        return num;
    }

    /**
     * Dispatch the message to the appropriate handler in the controller.
     * @param message The message received from the client socket.
     * @throws IOException
     */
    private void processMessage(String message) throws IOException {
        Integer code;

        log.info("Message: <"+message+">");

        code = getCode(message);
        if (code == null || code < 200 || code >= 500) {
            log.info("Invalid code: " + message);
            code = 0;
        }

        switch (code) {
            // These messages are direct responses to commands we have
            // sent to the client (OVER, INTO, RUN, SETB, etc)
            case 200:   // OK
            case 201:   // Started
            case 204:   // Output
                messageOK(code, message);
                break;

            // These messages are generated by the client on its own,
            // not in response to commands we send.
            case 202:   // Paused (Breakpoint)
                messageBreakpoint(message);
                break;

            case 203:   // Paused (Watchpoint)
                messageWatchpoint(message);
                break;

            case 400:
            case 401:
                messageError(code, message);
                break;

            default:
                // TODO: Recovery
                break;
        }

        // Schedule the next request in the queue
        scheduleNextRequest();
    }

    //////////////////////////////
    // Remote Responses

    private void messageWatchpoint(String message) throws IOException {
        Matcher m = RESPONSE_WP.matcher(message);

        if (m.matches()) {
            String file = m.group(1);
            String line = m.group(2);
            String watchIdx = m.group(3);

            String stack = reader.readLine();

            log.debug(String.format("watch <%s> at <%s> line <%s> stack <%s>", watchIdx, file, line, stack));

            LuaPosition position = new LuaPosition(file, Integer.parseInt(line));

            XBreakpoint bp = myPos2Breakpoints.get(position);

            if (bp != null) {
                // Breakpoint fired
                LuaSuspendContext ctx = new LuaSuspendContext(myProject, this, bp, stack);
                session.breakpointReached(bp, null, ctx);
            } else {
                // Watchpoint fired / Step completed
                XSourcePosition sp = LuaPositionConverter.createLocalPosition(position);
                LuaSuspendContext ctx = new LuaSuspendContext(myProject, this, sp, stack);
                session.positionReached(ctx);
            }
        }
    }

    private void messageBreakpoint(String message) throws IOException {
        Matcher m = RESPONSE_BP.matcher(message);

        if (m.matches()) {
            String file = m.group(1);
            String line = m.group(2);

            String stack = reader.readLine();

            log.debug(String.format("break at <%s> line <%s> stack <%s>", file, line, stack));

            LuaPosition position = new LuaPosition(file, Integer.parseInt(line));

            XBreakpoint bp = myPos2Breakpoints.get(position);

            if (bp != null) {
                // Breakpoint fired
                LuaSuspendContext ctx = new LuaSuspendContext(myProject, this, bp, stack);
                session.breakpointReached(bp, null, ctx);
            } else {
                // Watchpoint fired / Step completed
                XSourcePosition sp = LuaPositionConverter.createLocalPosition(position);
                LuaSuspendContext ctx = new LuaSuspendContext(myProject, this, sp, stack);
                session.positionReached(ctx);
            }
        }
    }

    private void messageOK(Integer code, String message) throws IOException {
        DebugRequest debugRequest = pendingRequests.poll();
        if (debugRequest != null) {
            switch (code) {
                case 200:
                    // "200 OK "
                    message = message.substring(Math.min(7, message.length()));
                    break;

                case 201:
                    // "201 Started "
                    message = message.substring(Math.min(11, message.length()));
                    break;
            }

            log.debug(String.format("Processing OK Payload: <%s>", message));

            // Hand the request the response value, and the
            // capability to read more characters
            debugRequest.completed(message, reader);
        }
    }

    private void messageError(Integer code, String message) throws IOException {
        // Cancel any request that this was in response to
        DebugRequest debugRequest = pendingRequests.peek();
        if (debugRequest != null && debugRequest.isInProgress()) {
            debugRequest = pendingRequests.poll();
            debugRequest.setInProgress(false);
        }

        // 400 errors don't have trailing data
        if (code == 400) {
            log.info("Client returned '400 Bad Request'");
            if (debugRequest != null)
                debugRequest.failed("Internal Error", reader);
            return;
        }

        // 401 errors always end in a length of the trailing data
        Matcher m = RESPONSE_ERR.matcher(message);
        if (m.matches()) {
            String errorText = m.group(1);
            String lengthText = m.group(2);
            Integer length = Integer.parseInt(lengthText);

            String errorMessage = reader.readBytes(length);
            log.debug(String.format("Client returned '%d %s': %s", code, errorText, errorMessage));

            if (debugRequest != null)
                debugRequest.failed(errorMessage, reader);
        }
    }

    private void scheduleNextRequest() {
        ready = true;

        // Since we are running in the Reader thread, set a callback from
        // somewhere else to continue with the next request
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                LuaDebuggerController.this.startPendingRequest();
            }
        });
    }

    interface DebugRequest {
        // Retrieve the debug protocol command line to send to the client
        String  getCommand();
        // Check if the request is being serviced
        boolean isInProgress();
        // Set if the request is being serviced
        void    setInProgress(boolean inProgress);
        // Process the successful client response to the command
        void    completed(String message, SocketReader socketReader) throws IOException;
        // Process the failed client response to the command
        void    failed(String message, SocketReader socketReader) throws IOException;
    }

    abstract static class DebugRequestBase implements DebugRequest {
        private boolean inProgress = false;

        public boolean isInProgress() {
            return inProgress;
        }

        public void setInProgress(boolean inProgress) {
            this.inProgress = inProgress;
        }
    }

    static class SimpleCommandRequest extends DebugRequestBase {
        private final String myCommand;

        public SimpleCommandRequest(String command) {
            myCommand = command;
        }

        public String getCommand() {
            return String.format("%s\n", myCommand);
        }

        @Override
        public void completed(String message, SocketReader socketReader) throws IOException { }

        @Override
        public void failed(String message, SocketReader socketReader) throws IOException { }
    }

    static class StackRequest extends DebugRequestBase {
        private final AsyncPromise<List<LuaDebugVariable>> myPromise;
        private final int myFrameIndex;

        StackRequest(AsyncPromise<List<LuaDebugVariable>> promise, int frameIndex) {
            myPromise = promise;
            myFrameIndex = frameIndex;
        }

        public String getCommand() {
            return "STACK\n";
        }

        public void completed(String message, SocketReader socketReader) throws IOException {
            // Parse the stack frames
            try {
                Globals globals = JsePlatform.debugGlobals();
                LuaValue chunk = globals.load(message);
                LuaTable stackDump = chunk.call().checktable();  // Executes the chunk and returns it

                // Convert the stack frame at <myIndex>
                final int index = stackDump.keyCount() - (myFrameIndex - 1);
                final LuaValue stackValueAtLevel = stackDump.get(index);
                final LuaTable stackAtLevel = stackValueAtLevel.checktable();
                final List<LuaDebugVariable> result = new LinkedList<LuaDebugVariable>();

                // Convert the local variables
                final LuaTable localsAtLevel = stackAtLevel.get(2).checktable();
                for (LuaValue key : localsAtLevel.keys()) {
                    final LuaTable variableInfo = localsAtLevel.get(key).checktable();
                    result.add(convertVariable(key, variableInfo));
                }

                // Convert the upvalues
                final LuaTable upvaluesAtLevel = stackAtLevel.get(3).checktable();
                for (LuaValue key : upvaluesAtLevel.keys()) {
                    final LuaTable variableInfo = upvaluesAtLevel.get(key).checktable();
                    result.add(convertVariable(key, variableInfo));
                }

                // Send the roots to the promise.
                myPromise.setResult(result);
            } catch (LuaError e) {
                myPromise.setError(e);
            }
        }

        private LuaDebugVariable convertVariable(LuaValue key, LuaTable variableInfo) {
            final LuaValue rawValue = variableInfo.get(1);
            final LuaDebugValue debugValue = new LuaDebugValue(rawValue, AllIcons.Nodes.Variable);
            return new LuaDebugVariable(
                    key.toString(),
                    debugValue
            );
        }

        @Override
        public void failed(String message, SocketReader socketReader) throws IOException { }
    }

    static class CodeExecutionRequest extends DebugRequestBase {
        private final AsyncPromise<LuaDebugValue> myPromise;
        private final String                      myCode;

        CodeExecutionRequest(AsyncPromise<LuaDebugValue> promise, String code) {
            myPromise = promise;
            myCode = code;
        }

        public String getCommand() {
            return String.format("EXEC %s\n", myCode);
        }

        public void completed(String message, SocketReader socketReader) throws IOException {
            final int valueSize = Integer.parseInt(message);
            final String valueString = socketReader.readBytes(valueSize);
            try {
                Globals globals = JsePlatform.debugGlobals();
                LuaValue chunk = globals.load(valueString);
                LuaTable stackDump = chunk.call().checktable();  // Executes the chunk and returns it
                LuaValue rawValue = stackDump;
                if (stackDump.keyCount() == 1)
                    rawValue = stackDump.get(1);
                else if (stackDump.keyCount() == 0)
                    rawValue = LuaValue.NIL;
                LuaDebugValue value = new LuaDebugValue(rawValue, AllIcons.Debugger.Watch);
                myPromise.setResult(value);
            } catch (LuaError e) {
                LuaDebugValue errorValue = new LuaDebugValue(
                        "error",
                        "Error during evaluation: " + e.getMessage(),
                        AllIcons.Nodes.ErrorMark
                );
                myPromise.setResult(errorValue);
            }
        }

        public void failed(String message, SocketReader socketReader) throws IOException {
            LuaDebugValue errorValue = new LuaDebugValue(
                    "error",
                    "Error during evaluation: " + message,
                    AllIcons.Ide.Error
            );
            myPromise.setResult(errorValue);
        }
    }
}
