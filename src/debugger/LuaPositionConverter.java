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

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XSourcePositionImpl;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/3/11
 * Time: 12:21 PM
 */
public class LuaPositionConverter {

    public static LuaPosition createRemotePosition(XSourcePosition xSourcePosition)
    {
        LuaPosition pos;

        assert xSourcePosition != null;

        pos = new LuaPosition(xSourcePosition.getFile().getPath(), xSourcePosition.getLine() + 1);

        return pos;
    }

    public static XSourcePosition createLocalPosition(LuaPosition luaPosition)
    {
        assert luaPosition != null;

        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(luaPosition.getPath());

        return XSourcePositionImpl.create(file, luaPosition.getLine() - 1);
    }
}
