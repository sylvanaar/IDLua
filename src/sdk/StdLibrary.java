/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.sdk;

import com.intellij.openapi.vfs.*;
import com.sylvanaar.idea.Lua.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/9/11
 * Time: 3:07 AM
 */
public class StdLibrary {
    public static final String STDLIBRARY    = "stdlibrary";
    public static final String DEBUG_LIBRARY = "mobdebug";
    public static final String LISTING_GENERATOR = "listing";

    private static VirtualFile getPluginChild(String ...args) {
        VirtualFile dir = LuaFileUtil.getPluginVirtualDirectory();
        for (String arg : args) {
            if (dir == null) break;
            dir = dir.findChild(arg);
        }
        return dir;
    }

    public static VirtualFile getStdFileLocation() {
        VirtualFile dir = getPluginChild(STDLIBRARY);
        if (dir == null)
            dir = getPluginChild("classes", STDLIBRARY);

        return dir;
    }

    public static VirtualFile getDebugModuleLocation() {
        VirtualFile dir = getPluginChild(DEBUG_LIBRARY);
        if (dir == null)
            dir = getPluginChild("classes", DEBUG_LIBRARY);
        return dir;
    }

    public static VirtualFile getListingModuleLocation() {
        VirtualFile dir = getPluginChild(LISTING_GENERATOR);
        if (dir == null)
            dir = getPluginChild("classes", LISTING_GENERATOR);
        return dir;
    }
}
