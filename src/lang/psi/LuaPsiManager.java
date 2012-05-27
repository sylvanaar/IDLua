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

import com.intellij.openapi.application.*;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.*;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.*;
import com.intellij.psi.search.*;
import com.intellij.util.*;
import com.intellij.util.concurrency.*;
import com.intellij.util.messages.*;
import com.sylvanaar.idea.Lua.lang.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.resolve.*;
import com.sylvanaar.idea.Lua.options.*;
import com.sylvanaar.idea.Lua.util.*;

import java.util.*;
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

    private static final ArrayList<LuaDeclarationExpression> EMPTY_CACHE = new ArrayList<LuaDeclarationExpression>();

    public Collection<LuaDeclarationExpression> getFilteredGlobalsCache() {
        try {
            if (filteredGlobalsCache == null)
                filteredGlobalsCache =
                        ApplicationManager.getApplication().executeOnPooledThread(new GlobalsCacheBuilder(project));

            return filteredGlobalsCache.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.info("exception creating globals cache", e);
        } catch (ExecutionException e) {
            log.info("exception creating globals cache", e);
        } catch (TimeoutException e) {
            log.info("The global cache is still processing");
        } catch (NullPointerException e) {
            log.info("Null cache");
        }

        return EMPTY_CACHE;
    }

    MessageBus myMessageBus = MessageBusFactory.newMessageBus(this);

    public LuaPsiManager(final Project project) {
        log.debug("*** CREATED ***");

        this.project = project;

        inferenceQueueProcessor =
                new QueueProcessor<InferenceCapable>(new InferenceQueue(project),  project.getDisposed(), false);

        DumbService.getInstance(project).runWhenSmart(new Runnable() {
            @Override
            public void run() {
                init(project);
            }
        });
    }

    private void reset() {
        log.debug("*** RESET ***");
        filteredGlobalsCache = null;
        inferProjectFiles(project);
    }

    private void init(final Project project) {
        log.debug("*** INIT ***");
        myMessageBus.connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener() {
            @Override
            public void beforePsiChanged(boolean isPhysical) {

            }

            @Override
            public void afterPsiChanged(boolean isPhysical) {
                if (filteredGlobalsCache != null) reset();
            }
        });
        filteredGlobalsCache =
                ApplicationManager.getApplication().executeOnPooledThread(new GlobalsCacheBuilder(project));


        inferAllTheThings(project);
        inferenceQueueProcessor.start();
    }

    private void inferAllTheThings(Project project) {
        if (!LuaApplicationSettings.getInstance().ENABLE_TYPE_INFERENCE) return;
        final ProjectRootManager m = ProjectRootManager.getInstance(project);
        final PsiManager p = PsiManager.getInstance(project);

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                m.orderEntries().forEach(new OrderEntryProcessor(p));
            }
        });
    }

    private void inferProjectFiles(Project project) {
        if (!LuaApplicationSettings.getInstance().ENABLE_TYPE_INFERENCE) return;
        final ProjectRootManager m = ProjectRootManager.getInstance(project);
        final PsiManager p = PsiManager.getInstance(project);

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                m.orderEntries().withoutSdk().withoutLibraries().forEach(new OrderEntryProcessor(p));
            }
        });
    }


    final QueueProcessor<InferenceCapable> inferenceQueueProcessor;

    final LinkedList<InferenceCapable> work = new LinkedList<InferenceCapable>();


    public void queueInferences(InferenceCapable a) {
        if (!LuaApplicationSettings.getInstance().ENABLE_TYPE_INFERENCE) return;

        synchronized (work) {
            if (work.contains(a)) {
                log.debug("Already processing " + a);
                return;
            }
            inferenceQueueProcessor.add(a);
        }
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
            DumbService.getInstance(project).waitForSmartMode();
            return ApplicationManager.getApplication()
                                     .runReadAction(new Computable<Collection<LuaDeclarationExpression>>() {

                                         @Override
                                         public Collection<LuaDeclarationExpression> compute() {
                                             return ResolveUtil.getFilteredGlobals(project,
                                                     new ProjectAndLibrariesScope(project));
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

            final LuaPsiManager psiManager = LuaPsiManager.getInstance(project);
            if (project.isDisposed())
                return;
            if (DumbService.isDumb(project)) {
                log.debug("inference q not ready");
                psiManager.queueInferences(element);
                return;
            }

            ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!element.isValid()) {
                            log.debug("invalid element ");
                            return;
                        }
                        log.debug("inference: " + element.toString());


                        element.inferTypes();
                    } finally {
                        synchronized (psiManager.work) {
                            psiManager.work.remove(element);
                        }
                    }

                }
            });
        }
    }

    private class OrderEntryProcessor implements Processor<OrderEntry> {
        private final PsiManager p;
        final boolean projectOnly;

        public OrderEntryProcessor(PsiManager p, boolean projectOnly) {
            this.p = p;
            this.projectOnly = projectOnly;
        }
        public OrderEntryProcessor(PsiManager p) {
            this(p, false);
        }

        @Override
        public boolean process(OrderEntry orderEntry) {
            for (final VirtualFile f : orderEntry.getFiles(OrderRootType.CLASSES)) {
                LuaFileUtil.iterateRecursively(f, new ContentIterator() {
                    @Override
                    public boolean processFile(VirtualFile fileOrDir) {
                        ProgressManager.checkCanceled();

                        final FileViewProvider viewProvider = p.findViewProvider(fileOrDir);
                        if (viewProvider == null) return true;

                        final PsiFile psiFile = viewProvider.getPsi(viewProvider.getBaseLanguage());
                        if (!(psiFile instanceof InferenceCapable)) return true;

                        log.debug("forcing inference for: " + fileOrDir.getName());

                        final InferenceCapable psi = (InferenceCapable) psiFile;
                        inferenceQueueProcessor.add(psi);

                        return true;
                    }
                });
            }
            return true;
        }
    }
}
