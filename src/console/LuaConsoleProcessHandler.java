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
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;

import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/20/11
 * Time: 4:55 PM
 */
public class LuaConsoleProcessHandler extends ColoredProcessHandler
{

    public LuaConsoleProcessHandler(Process process, LanguageConsoleImpl languageConsole, String commandLine, Charset charset)
    {
        super(process, commandLine, charset);
        myLanguageConsole = languageConsole;
    }

    protected void textAvailable(String text, Key attributes)
    {
        String string = processPrompts(myLanguageConsole, StringUtil.convertLineSeparators(text));
        
        myLanguageConsole.queueUiUpdate(true);
    }

    private String processPrompts(LanguageConsoleImpl languageConsole, String string)
    {
        String arr$[] = PROMPTS;
        int len$ = arr$.length;
        int i$ = 0;
        do
        {
            if(i$ >= len$)
                break;
            String prompt = arr$[i$];
            if(string.startsWith(prompt))
            {
                StringBuilder builder = new StringBuilder();
                builder.append(prompt).append(prompt);
                for(; string.startsWith(builder.toString()); builder.append(prompt));
                String multiPrompt = builder.toString().substring(prompt.length());
                if(prompt == ">> ")
                    prompt = multiPrompt;
                string = string.substring(multiPrompt.length());

                String currentPrompt = languageConsole.getPrompt();
                String trimmedPrompt = prompt.trim();
                if(!currentPrompt.equals(trimmedPrompt))
                    languageConsole.setPrompt(trimmedPrompt);
                break;
            }
            i$++;
        } while(true);
        return string;
    }

    private final LanguageConsoleImpl myLanguageConsole;
    private final String PROMPTS[] = {
        "> ", ">> "
    };
}

