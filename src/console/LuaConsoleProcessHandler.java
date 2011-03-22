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

import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Key;

import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/20/11
 * Time: 4:55 PM
 */
public class LuaConsoleProcessHandler extends ColoredProcessHandler {

    public LuaConsoleProcessHandler(Process process, LanguageConsoleImpl languageConsole, String commandLine,
                                    Charset charset) {
        super(process, commandLine, charset);
        myLanguageConsole = languageConsole;
    }

    protected void textAvailable(String text, Key attributes) {
        ConsoleViewContentType outputType;
        if (attributes == ProcessOutputTypes.STDERR) outputType = ConsoleViewContentType.ERROR_OUTPUT;
        else if (attributes == ProcessOutputTypes.SYSTEM) outputType = ConsoleViewContentType.SYSTEM_OUTPUT;
        else outputType = ConsoleViewContentType.NORMAL_OUTPUT;

        if (text.startsWith(">>")) {
            text = text.substring(3);
            myLanguageConsole.setPrompt(">>");
        } else if (text.startsWith(">")) {
            text = text.substring(2);
            myLanguageConsole.setPrompt(">");
        }

        if (outputType != ConsoleViewContentType.SYSTEM_OUTPUT)
            LanguageConsoleImpl.printToConsole(myLanguageConsole, text, outputType, null);

        myLanguageConsole.queueUiUpdate(true);
    }


    private final LanguageConsoleImpl myLanguageConsole;
}

