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
import com.sylvanaar.idea.Lua.util.LuaStringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    OutputStream outputStream = null;

    static final String STEP = "STEP\n";
    private boolean readerCanRun = true;

    public void waitForConnect() throws IOException {
        log.info("Starting Debug Controller");
        serverSocket = new ServerSocket(serverPort);

        log.info("Accepting Connections");
        clientSocket = serverSocket.accept();
        log.info("Client Connected " + clientSocket.getInetAddress());

        reader = new SocketReader();
        reader.start();

        outputStream = clientSocket.getOutputStream();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        log.info(LuaStringUtil.getHex(STEP.getBytes("UTF8")));
        try {
            outputStream.write(STEP.getBytes("UTF8"));
            //outputStream.flush();
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
                    if (input.read(buffer) > 0) log.info(new String(buffer));
                        else log.info("No data to read");
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

}
