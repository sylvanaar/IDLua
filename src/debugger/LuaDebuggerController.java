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

import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

    static final String STEP = "STEP\\n";
    private boolean readerCanRun = true;

    public void waitForConnect() throws IOException {
        log.info("Starting Debug Controller");
        serverSocket = new ServerSocket(serverPort);

        log.info("Accepting Connections");
        clientSocket = serverSocket.accept();

        reader = new SocketReader();
        reader.start();

        log.info("Client Connected");
        clientSocket.getOutputStream().write(STEP.getBytes("UTF8"));
        clientSocket.getOutputStream().flush();
    }

    public void terminate() {
        readerCanRun = false;

        try {
            serverSocket.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    class SocketReader extends Thread {
        public SocketReader() {
            super("DebuggerSocketReader");
        }

        @Override
        public void run() {
            log.info("Read thread started");

            byte[] buffer = new byte[1000];

            while (true && readerCanRun) {
                try {
                    BufferedInputStream input = new BufferedInputStream(clientSocket.getInputStream());
                    while (input.available() > 0) {
                        input.read(buffer);

                        log.info(buffer.toString());
                    }
                    sleep(500);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();  
                    break;
                }

            }

        }
    }

}
