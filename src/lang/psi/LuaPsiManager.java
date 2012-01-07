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
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.QueueProcessor;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusFactory;
import com.sylvanaar.idea.Lua.lang.InferenceCapable;
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
    private static final Logger log = Logger.getInstance("Lua.LuaPsiManger");

    private Future<Collection<LuaDeclarationExpression>> filteredGlobalsCache = null;
    private final Project project;

    public Collection<LuaDeclarationExpression> getFilteredGlobalsCache() {
        try {
            return filteredGlobalsCache.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.info("exception creating globals cache", e);
        } catch (ExecutionException e) {
            log.info("exception creating globals cache", e);
        } catch (TimeoutException e) {
            log.info("The global cache is still processing");
        }

        return new ArrayList<LuaDeclarationExpression>();
    }

    MessageBus myMessageBus = MessageBusFactory.newMessageBus(this);

    public LuaPsiManager(final Project project) {
        log.info("*** CREATED ***");

        this.project = project;

        inferenceQueueProcessor = new QueueProcessor<InferenceCapable>(new InferenceQueue(project),
                Condition.FALSE, false);

        StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
            @Override
            public void run() {
                startup(project);
            }
        });
    }

    private void startup(final Project project) {
        log.info("*** STARTUP ***");

        DumbService.getInstance(project).runWhenSmart(new Runnable() {
            @Override
            public void run() {
                init(project);
            }
        });
    }

    private void reset() {
        log.info("*** RESET ***");
        filteredGlobalsCache = ApplicationManager.getApplication().executeOnPooledThread(new GlobalsCacheBuilder(project));
        inferenceQueueProcessor.clear();
    }

    private void init(Project project) {
        log.info("*** INIT ***");
        myMessageBus.connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener() {
            @Override
            public void beforePsiChanged(boolean isPhysical) {

            }

            @Override
            public void afterPsiChanged(boolean isPhysical) {
                if (filteredGlobalsCache != null)
                    reset();
            }
        });
        filteredGlobalsCache = ApplicationManager.getApplication().executeOnPooledThread(new GlobalsCacheBuilder(project));
        inferenceQueueProcessor.start();
    }

    QueueProcessor<InferenceCapable> inferenceQueueProcessor;

    public void queueInferences(InferenceCapable a) {
        inferenceQueueProcessor.add(a);
    }

    public static LuaPsiManager getInstance(Project project) {
        return ServiceManager.getService(project, LuaPsiManager.class);
    }

    static class GlobalsCacheBuilder implements Callable<Collection<LuaDeclarationExpression>> {
        private final Project project;

        public GlobalsCacheBuilder(Project project) {
            this.project = project;
        }

        @Override
        public Collection<LuaDeclarationExpression> call() throws Exception {
            return ApplicationManager.getApplication().runReadAction(new Computable<Collection<LuaDeclarationExpression>>() {

                @Override
                public Collection<LuaDeclarationExpression> compute() {
                    DumbService.getInstance(project).waitForSmartMode();
                    return ResolveUtil.getFilteredGlobals(project, new ProjectAndLibrariesScope(project));
                }
            });
        }
    }

    private static class InferenceQueue implements Consumer<InferenceCapable> {
        private final Project project;

        public InferenceQueue(Project project) {
            assert project != null : "Project is null";

            this.project = project;
        }

        @Override
        public void consume(final InferenceCapable element) {

            if (DumbService.isDumb(project)) {
                log.debug("inference q not ready");
                LuaPsiManager.getInstance(project).queueInferences(element);
                return;
            }

            ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                    if (element.isValid()) return;
                    element.inferTypes();
                }
            });
        }
    }
}
