/*
 * Copyright 2012 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.luaj;

import com.intellij.openapi.project.*;
import com.intellij.openapi.wm.*;
import se.krka.kahlua.converter.*;
import se.krka.kahlua.j2se.*;
import se.krka.kahlua.vm.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: May 7, 2010
 * Time: 8:02:20 PM
 */
public class LuaJInterpreterWindowFactory implements ToolWindowFactory {
    public static LuaJInterpreter INSTANCE = null;
    public static ToolWindow WINDOW = null;

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
//        final Platform platform = new J2SEPlatform();
//        final KahluaTable env = platform.newEnvironment();

//        KahluaConverterManager manager = new KahluaConverterManager();
//        KahluaNumberConverter.install(manager);
//        KahluaEnumConverter.install(manager);
//        new KahluaTableConverter(platform).install(manager);

//        KahluaTable staticBase = platform.newTable();
//        env.rawset("Java", staticBase);

        LuaJInterpreter shell = new LuaJInterpreter();

        INSTANCE = shell;

        shell.getTerminal().appendInfo("Useful shortcuts:\n" +
                "Ctrl-enter -- execute script\n" +
                "Ctrl-space -- autocomplete global variables\n" +
                "Ctrl-p -- show definition (if available)\n" +
                "Ctrl-up/down -- browse input history\n" +
                ""
        );

        WINDOW = toolWindow;
        toolWindow.getComponent().add(shell);
    }
}
