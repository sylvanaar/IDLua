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

package com.sylvanaar.idea.Lua.run.luaj;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.sylvanaar.idea.Lua.run.lua.LuaProcessHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 19, 2010
 * Time: 3:06:41 PM
 */
public class LuaJExternalProcessHandler extends LuaProcessHandler {
    private static final Logger log = Logger.getInstance("Lua.LuaJExternalProcessHandler");

    public LuaJExternalProcessHandler(@NotNull Process process, @Nullable String commandLine) {
        super(process, commandLine);
    }

    @Override
    public void notifyTextAvailable(String text, Key outputType) {
    }
}
