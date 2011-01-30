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
package com.sylvanaar.idea.Lua.findUsages;

import com.intellij.concurrency.JobUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.search.LowLevelSearchUtil;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.intellij.util.text.StringSearcher;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LuaIdentifierSearch implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters>
{
  public LuaIdentifierSearch()
  {
   // AsyncResult<DataContext> dataContext = DataManager.getInstance().getDataContextFromFocus();
   // Project project = DataKeys.PROJECT.getData(dataContext);
  }

  public boolean execute(@NotNull final ReferencesSearch.SearchParameters queryParameters,
                         @NotNull final Processor<PsiReference> consumer)
  {
    final PsiElement refElement = queryParameters.getElementToSearch();
    Project project = refElement.getProject();
    PsiManagerEx manager = (PsiManagerEx) PsiManager.getInstance(project);

    if (refElement instanceof LuaIdentifier)
    {
      final String name = ((LuaIdentifier) refElement).getName();
      if (name == null)
      {
        return true;
      }

      SearchScope searchScope = ApplicationManager.getApplication().runReadAction(new Computable<SearchScope>()
      {
        public SearchScope compute()
        {
          return queryParameters.getEffectiveSearchScope();
        }
      });

      final TextOccurenceProcessor processor = new TextOccurenceProcessor()
      {
        public boolean execute(PsiElement element, int offsetInElement)
        {
          ProgressManager.checkCanceled();
          PsiReference ref = element.getReference();
            return !((ref != null) && ref.getRangeInElement().contains(offsetInElement) && ref.isReferenceTo(refElement)) || consumer.process(ref);
        }
      };

      short
        searchContext =
        UsageSearchContext.IN_CODE | UsageSearchContext.IN_FOREIGN_LANGUAGES | UsageSearchContext.IN_COMMENTS;

      final boolean caseSensitively = false;

      if (searchScope instanceof GlobalSearchScope)
      {
        StringSearcher searcher = new StringSearcher(name, caseSensitively, true);

        return processElementsWithTextInGlobalScope(processor,
                                                    (GlobalSearchScope) searchScope,
                                                    searcher,
                                                    searchContext,
                                                    caseSensitively,
                                                    manager);
      }
      else
      {
        LocalSearchScope scope = (LocalSearchScope) searchScope;
        PsiElement[] scopeElements = scope.getScope();
        final boolean ignoreInjectedPsi = scope.isIgnoreInjectedPsi();

        for (final PsiElement scopeElement : scopeElements)
        {
          return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
              public Boolean compute() {
                  StringSearcher searcher = new StringSearcher(name, caseSensitively, true);

                  ProgressManager progressManager = ProgressManager.getInstance();

                  return LowLevelSearchUtil.processElementsContainingWordInElement(processor,
                          scopeElement,
                          searcher,
                          ignoreInjectedPsi,
                          progressManager.getProgressIndicator());
              }
          });
        }
        return true;
      }
    }

    return true;
  }

  private boolean processElementsWithTextInGlobalScope(final TextOccurenceProcessor processor,
                                                       final GlobalSearchScope scope,
                                                       final StringSearcher searcher,
                                                       final short searchContext,
                                                       final boolean caseSensitively,
                                                       final PsiManagerEx manager)
  {
    final ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
    if (progress != null)
    {
      progress.pushState();
      progress.setText(PsiBundle.message("psi.scanning.files.progress"));
    }
    manager.startBatchFilesProcessingMode();

    try
    {
      final String word = searcher.getPattern();
      final Application application = ApplicationManager.getApplication();
      Collection<PsiFile> fileSet = application.runReadAction(new Computable<Collection<PsiFile>>()
      {
        public Collection<PsiFile> compute()
        {
          return Arrays.asList(manager.getCacheManager().getFilesWithWord(word, searchContext, scope, caseSensitively));
        }
      });

      if (progress != null)
      {
        progress.setText(PsiBundle.message("psi.search.for.word.progress", word));
      }

      final AtomicInteger counter = new AtomicInteger(0);
      final AtomicBoolean canceled = new AtomicBoolean(false);
      final AtomicBoolean pceThrown = new AtomicBoolean(false);

      List<PsiFile> psiFiles = new ArrayList<PsiFile>(fileSet);
      final int size = psiFiles.size();
      boolean completed = JobUtil.invokeConcurrentlyUnderProgress(psiFiles, new Processor<PsiFile>()
      {
        public boolean process(final PsiFile file)
        {
          if (file instanceof PsiBinaryFile)
          {
            return true;
          }
          application.runReadAction(new Runnable()
          {
            public void run()
            {
              try
              {
                List<PsiFile> psiRoots = file.getViewProvider().getAllFiles();
                Set<PsiElement> processed = new HashSet<PsiElement>(psiRoots.size() * 2, (float) 0.5);
                for (PsiElement psiRoot : psiRoots)
                {
                  ProgressManager.checkCanceled();
                  if (!processed.add(psiRoot))
                  {
                    continue;
                  }
                  if (!LowLevelSearchUtil.processElementsContainingWordInElement(processor,
                                                                                 psiRoot,
                                                                                 searcher,
                                                                                 false,
                                                                                 progress))
                  {
                    canceled.set(true);
                    return;
                  }
                }
                if (progress != null)
                {
                  double fraction = (double) counter.incrementAndGet() / size;
                  progress.setFraction(fraction);
                }
                manager.dropResolveCaches();
              }
              catch (ProcessCanceledException e)
              {
                canceled.set(true);
                pceThrown.set(true);
              }
            }
          });
          return !canceled.get();
        }
      }, true, progress);

      if (pceThrown.get())
      {
        throw new ProcessCanceledException();
      }

      return completed;
    }
    finally
    {
      if (progress != null)
      {
        progress.popState();
      }
      manager.finishBatchFilesProcessingMode();
    }
  }
}
