package com.sylvanaar.idea.Lua.run;

import com.intellij.execution.filters.RegexpFilter;
import com.intellij.openapi.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 13.08.2009
 * Time: 18:27:37
 */
/**
 * Responsible for clickable configuration file references in console
 */
public class LuaConsoleFilter extends RegexpFilter {

    public LuaConsoleFilter(Project project) {
        super(project, ".+ in $FILE_PATH$:$LINE$");
    }
}
