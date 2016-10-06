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

package com.sylvanaar.idea.Lua.run.lua;

import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.openapi.vfs.CharsetToolkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 12/16/12
 * Time: 3:51 AM
 */
public class LuaProcessHandler extends CapturingProcessHandler {
    public LuaProcessHandler(@NotNull Process process, @Nullable String commandLine) {
        super(process, CharsetToolkit.UTF8_CHARSET,  commandLine);
    }

    @Override
    protected boolean shouldDestroyProcessRecursively() {
        return true;
    }


}
