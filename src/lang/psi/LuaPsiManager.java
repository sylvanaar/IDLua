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
import com.intellij.openapi.util.Computable;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 7/10/11
 * Time: 6:32 PM
 */
public class LuaPsiManager {
    private static final Logger LOG = Logger.getInstance("Lua.LuaPsiManger");

    private Future<Collection<LuaDeclarationExpression>> filteredGlobalsCache = null;

    public Collection<LuaDeclarationExpression> getFilteredGlobalsCache() {
        try {
            return filteredGlobalsCache.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.info("exception creating globals cache", e);
        } catch (ExecutionException e) {
            LOG.info("exception creating globals cache", e);
        } catch (TimeoutException e) {
            LOG.info("The global cache is still processing");
        }

        return new ArrayList<LuaDeclarationExpression>();
    }

    public LuaPsiManager(final Project project) {

        filteredGlobalsCache = ApplicationManager.getApplication().executeOnPooledThread(new GlobalsCacheBuilder(project));

    }

    public static LuaPsiManager getInstance(Project project) {
        return ServiceManager.getService(project, LuaPsiManager.class);
    }

    private static class GlobalsCacheBuilder implements Callable<Collection<LuaDeclarationExpression>> {
        private final Project project;

        public GlobalsCacheBuilder(Project project) {
            this.project = project;
        }

        @Override
        public Collection<LuaDeclarationExpression> call() throws Exception {
            return ApplicationManager.getApplication().runReadAction(new Computable<Collection<LuaDeclarationExpression>>() {

                @Override
                public Collection<LuaDeclarationExpression> compute() {
                    return ResolveUtil.getFilteredGlobals(project, new ProjectAndLibrariesScope(project));
                }
            });
        }
    }
}
