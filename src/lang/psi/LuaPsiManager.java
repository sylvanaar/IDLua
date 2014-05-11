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
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.project.DumbModeAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.util.Consumer;
import com.intellij.util.PathsList;
import com.intellij.util.Processor;
import com.intellij.util.concurrency.QueueProcessor;
import com.intellij.util.containers.ArrayListSet;
import com.sylvanaar.idea.Lua.LuaBundle;
import com.sylvanaar.idea.Lua.lang.InferenceCapable;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import com.sylvanaar.idea.Lua.options.LuaApplicationSettings;
import com.sylvanaar.idea.Lua.util.LuaAtomicNotNullLazyValue;
import com.sylvanaar.idea.Lua.util.LuaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 7/10/11
 * Time: 6:32 PM
 */
public class LuaPsiManager extends AbstractProjectComponent implements ProjectComponent {
    private static final Logger log = Logger.getInstance("Lua.LuaPsiManger");

//    private static final NotNullLazyKey<LuaPsiManager, Project> INSTANCE_KEY = ServiceManager.createLazyKey(
//            LuaPsiManager.class);

    private final LuaAtomicNotNullLazyValue<Future<Collection<LuaDeclarationExpression>>> filteredGlobalsCache =
            new LuaAtomicNotNullLazyValue<Future<Collection<LuaDeclarationExpression>>>() {
                @NotNull
                @Override
                protected Future<Collection<LuaDeclarationExpression>> compute() {
                    return ApplicationManager.getApplication()
                                             .executeOnPooledThread(new GlobalsCacheBuilder(myProject));
                }
            };

    private static final ArrayList<LuaDeclarationExpression> EMPTY_CACHE = new ArrayList<LuaDeclarationExpression>();

    public Collection<LuaDeclarationExpression> getFilteredGlobalsCache() {
        try {
            final Collection<LuaDeclarationExpression> cache =
                    filteredGlobalsCache.getValue().get(1000, TimeUnit.MILLISECONDS);

            if (cache != null) return cache;
        } catch (InterruptedException e) {
            log.info("exception creating globals cache", e);
        } catch (ExecutionException e) {
            log.info("exception creating globals cache", e);
        } catch (TimeoutException e) {
            log.info("The global cache is still processing");
        } catch (NullPointerException e) {
            log.info("Null cache");
        }

        filteredGlobalsCache.drop();
        return EMPTY_CACHE;
    }

    public LuaPsiManager(final Project project) {
        super(project);
        log.debug("*** CREATED ***");
    }

    private void reset() {
        log.debug("*** RESET ***");
        filteredGlobalsCache.drop();
    }

    private void init(final Project project) {
        log.debug("*** INIT ***");
        myProject.getMessageBus().connect(myProject)
                 .subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener() {
                     @Override
                     public void beforePsiChanged(boolean isPhysical) {
//                         reset();
                     }

                     @Override
                     public void afterPsiChanged(boolean isPhysical) {
                     }
                 });

       inferAllTheThings(project);
       inferenceQueueProcessor.start();
    }

    private void inferAllTheThings(Project project) {
        if (!isTypeInferenceEnabled()) return;
        final ProjectRootManager m = ProjectRootManager.getInstance(project);
        final PsiManager p = PsiManager.getInstance(project);

        ProgressManager.getInstance().run(new MyBackgroundableInferencer(project, m, p));
    }

    private void inferProjectFiles(Project project) {
        if (!isTypeInferenceEnabled()) return;
        final ProjectRootManager m = ProjectRootManager.getInstance(project);
        final PsiManager p = PsiManager.getInstance(project);

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                final PathsList pathsList = m.orderEntries().withoutSdk().withoutLibraries().sources().getPathsList();
                final List<VirtualFile> virtualFiles = pathsList.getVirtualFiles();

                for (VirtualFile file : virtualFiles) {
                    log.debug(file.getName());
                }
            }
        });
    }


    private QueueProcessor<InferenceCapable> inferenceQueueProcessor;

    private Set<InferenceCapable> work;


    public void queueInferences(InferenceCapable inference) {
        if (!isTypeInferenceEnabled()) return;

        synchronized (work) {
            if (work.contains(inference)) {
                log.debug("Already processing " + inference);
                return;
            }
            inferenceQueueProcessor.add(inference);
        }
    }

    public void queueInferences(Collection<InferenceCapable> inferences) {
        if (!isTypeInferenceEnabled()) return;

        synchronized (work) {
            for (InferenceCapable item : inferences) {
                if (work.contains(item)) {
                    log.debug("Already processing " + inferences);
                    continue;
                }
                inferenceQueueProcessor.add(item);
            }
        }
    }

    private static boolean isTypeInferenceEnabled() {
        if (ApplicationManager.getApplication().isUnitTestMode()) return false;
        return LuaApplicationSettings.getInstance().ENABLE_TYPE_INFERENCE;
    }

    public static LuaPsiManager getInstance(Project project) {
        return project.getComponent(LuaPsiManager.class);
//        return INSTANCE_KEY.getValue(project);
    }

    @Override
    public void projectOpened() {
        work = new ArrayListSet<InferenceCapable>();
        inferenceQueueProcessor =
                new QueueProcessor<InferenceCapable>(new InferenceQueue(myProject), myProject.getDisposed(), false);



        StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
            @Override
            public void run() {
                DumbService.getInstance(myProject).runWhenSmart(new InitRunnable());
            }
        });
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Lua.PsiManager";
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

    private class InferenceQueue implements Consumer<InferenceCapable> {
        private final Project project;

        public InferenceQueue(Project project) {
            assert project != null : "Project is null";

            this.project = project;
        }

        @Override
        public void consume(final InferenceCapable element) {
            ProgressManager.checkCanceled();

            if (project.isDisposed())
                return;
            if (DumbService.isDumb(project)) {
                log.debug("inference q not ready");
                DumbService.getInstance(project).waitForSmartMode();
            }


            ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!element.isValid()) {
                            log.debug("invalid element ");
                            return;
                        }

                        if (LuaPsiUtils.hasDirectChildErrorElements(element)) {
                            log.debug("error in element " + element);
                            return;
                        }
                        log.debug("inference: " + element.toString());

                        element.inferTypes();
                    } catch (ProcessCanceledException e) {
                        log.debug("Error Process Cancelled");
                    } finally {
                        synchronized (work) {
                            if (element instanceof LuaPsiFile)
                                fileCount.decrementAndGet();
                            work.remove(element);
                        }
                    }

                }
            });
        }
    }

    private AtomicInteger fileCount = new AtomicInteger(0);

    private class OrderEntryProcessor implements Processor<OrderEntry> {
        private final PsiManager p;
        private final ProgressIndicator indicator;

        public OrderEntryProcessor(PsiManager p, @Nullable ProgressIndicator indicator) {
            this.indicator = indicator;
            this.p = p;
        }

        public OrderEntryProcessor(PsiManager p) {
            this(p, null);
        }

        @Override
        public boolean process(OrderEntry orderEntry) {
            log.debug("process " + orderEntry.getPresentableName());
            final List<InferenceCapable> files = new ArrayList<InferenceCapable>();
            for (final VirtualFile f : orderEntry.getFiles(OrderRootType.SOURCES)) {
                log.debug("process class " + f.getName());
                processRoot(files, f);
            }
            if (orderEntry instanceof ModuleSourceOrderEntry)
                for (final VirtualFile f : ((ModuleSourceOrderEntry) orderEntry).getRootModel().getContentRoots()) {
                    log.debug("process source" + f.getName());
                    processRoot(files, f);
                }

            fileCount.getAndAdd(files.size());
            queueInferences(files);
            log.debug("order entries processed");
            return true;
        }

        private void processRoot(final List<InferenceCapable> files, VirtualFile f) {
            LuaFileUtil.iterateLuaFilesRecursively(f, new ContentIterator() {
                @Override
                public boolean processFile(VirtualFile fileOrDir) {
                    ProgressManager.checkCanceled();

                    log.debug("process " + fileOrDir.getName());
                    if (fileOrDir.isDirectory()) return true;

                    indicator.setText2(fileOrDir.getPresentableName());
                    final FileViewProvider viewProvider = p.findViewProvider(fileOrDir);
                    if (viewProvider == null) return true;

                    final PsiFile psiFile = viewProvider.getPsi(viewProvider.getBaseLanguage());
                    if (!(psiFile instanceof InferenceCapable)) return true;

                    indicator.setText2(fileOrDir.getName());
                    log.debug("forcing inference for: " + fileOrDir.getName());

                    files.add((InferenceCapable) psiFile);

                    return true;
                }
            });
        }
    }

    private class InitRunnable implements Runnable {
        @Override
        public void run() {
            final DumbService dumbService = DumbService.getInstance(myProject);
            if (dumbService.isDumb()) dumbService.runWhenSmart(new InitRunnable());
            else init(myProject);
        }
    }

    private class MyBackgroundableInferencer extends Task.Backgroundable {

        private final ProjectRootManager m;
        private final PsiManager         p;

        public MyBackgroundableInferencer(Project project, ProjectRootManager m, PsiManager p) {
            super(project, LuaBundle.message("inferrencer.first.run"), true, PerformInBackgroundOption.DEAF);
            this.m = m;
            this.p = p;
        }

        @Override
        public void run(@NotNull final ProgressIndicator indicator) {
            ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                    indicator.setText(LuaBundle.message("inferrencer.finding.files"));
                    m.orderEntries().forEach(new OrderEntryProcessor(p, indicator));
                    indicator.setText2("");
                    indicator.setText(LuaBundle.message("inferrencer.working"));
                }
            });

            final double max = fileCount.get();

            
            while (!inferenceQueueProcessor.isEmpty()) {
                try {
                    indicator.checkCanceled();
                    indicator.setFraction((max - fileCount.get()) / max);
                    Thread.sleep(100);
                } catch (ProcessCanceledException e) {
                    inferenceQueueProcessor.clear();
                    return;
                } catch (InterruptedException ignored) {
                    return;
                }
            }

            indicator.setFraction(1);
        }

        @Override
        public boolean shouldStartInBackground() {
            return true;
        }

        @Override
        public DumbModeAction getDumbModeAction() {
            return DumbModeAction.WAIT;
        }
    }
}
