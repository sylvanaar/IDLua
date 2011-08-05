/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.run;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.RegexpFilter;
import com.intellij.openapi.project.Project;


public class LuaLineErrorFilter extends RegexpFilter implements Filter {
    //e.g. E:/IdeaProjects/IDLua/src/test.lua:17:
    // E:\Lua\5.1\lua.exe: E:/IdeaProjects/IDLua/test-new.lua:52: bad argument #1 to 'pairs' (table expected, got nil)
    private static final String FILTER_REGEXP = /* "^[^:]+:[^:]+: "*/
            "\\s*" + RegexpFilter.FILE_PATH_MACROS + ":" + RegexpFilter.LINE_MACROS;// + ":";

    public LuaLineErrorFilter(Project project) {
        //: line (\d+):
        super(project, FILTER_REGEXP);
    }
}
