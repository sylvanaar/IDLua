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

package com.sylvanaar.idea.Lua.lang.psi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 7/10/11
 * Time: 6:32 PM
 */
public class LuaPsiManager {
    private static final Logger LOG = Logger.getInstance("#Lua.LuaPsiManger");

    private Project myProject = null;

    public Collection<String> filteredGlobalsCache = null;

    public LuaPsiManager(final Project project) {
        myProject = project;
        //StartupManager startup = StartupManagerEx.getInstanceEx(myProject);

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                filteredGlobalsCache = ResolveUtil.getFilteredGlobals(project, new ProjectAndLibrariesScope(project));
            }
        });
    }

    public static LuaPsiManager getInstance(Project project) {
        return ServiceManager.getService(project, LuaPsiManager.class);
    }

}
