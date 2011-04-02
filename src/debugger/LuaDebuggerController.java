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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/22/11
 * Time: 6:31 PM
 */
public class LuaDebuggerController {
    private static final Logger log = Logger.getInstance("#Lua.LuaDebuggerController");
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
    private XDebugSession session   ;
    private ConsoleView console;


    LuaDebuggerController(XDebugSession session) {
        this.session = session;
        this.session.setPauseActionSupported(false);
        AT_BREAKPOINT = Pattern.compile("^202 Paused\\s+(\\S+)\\s+(\\d+)\\n", Pattern.MULTILINE);
    }

    public void printToConsole(String text, ConsoleViewContentType contentType)
    {
        assert console != null;
        
        console.print(text, contentType);
    }
    
    public void waitForConnect() throws IOException {
        log.info("Starting Debug Controller");
        serverSocket = new ServerSocket(serverPort);

        log.info("Accepting Connections");
        clientSocket = serverSocket.accept();
        log.info("Client Connected " + clientSocket.getInetAddress());

        printToConsole("Debugger connected at " + clientSocket.getInetAddress(), ConsoleViewContentType.SYSTEM_OUTPUT);

        reader = new SocketReader();
        reader.start();

        outputStream = clientSocket.getOutputStream();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            outputStream.write(STEP.getBytes("UTF8"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void terminate() {
        readerCanRun = false;

        try {
            serverSocket.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();  
        }
    }

    public void stepInto() {
        try {
            outputStream.write(STEP.getBytes("UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stepOver() {
        try {
            outputStream.write(STEP_OVER.getBytes("UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        try {
            outputStream.write(RUN.getBytes("UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setConsole(ConsoleView console) {
        this.console = console;
    }

    public void addBreakPoint(XBreakpoint xBreakpoint) {
        
    }

    public void removeBreakPoint(XBreakpoint xBreakpoint) {

    }


    class SocketReader extends Thread {
        public SocketReader() {
            super("DebuggerSocketReader");
        }

        @Override
        public void run() {
            log.info("Read thread started");

            byte[] buffer = new byte[1000];
            InputStream input = null;
            try {
                input = clientSocket.getInputStream();


            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            while (readerCanRun) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                    break;
//                }


                try {
                    while (input.available() > 0) {
                        assert input != null;
                        int count = 0;
                        if ((count = input.read(buffer)) > 0)
                            processResponse(new String(buffer, 0, count, CharsetToolkit.UTF8));
                        else
                            log.info("No data to read");
                    }
//                    sleep(500);

//                    log.info(String.valueOf(input.read()));
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
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

    private void processResponse(String message) {
        log.info("Response: <"+message+">");

        Matcher m = AT_BREAKPOINT.matcher(message);

        if (m.matches()) {
            String file = cleanupFileName(m.group(1));
            String line = m.group(2);

            log.info(String.format("break at <%s> line <%s>", file, line));

//            session.updateExecutionPosition();

//
//            XSuspendContext context = new XSuspendContext() {};
//            session.breakpointReached(XBreakpoint)
            return;
        }
    }

}
