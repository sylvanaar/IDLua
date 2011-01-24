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

package com.sylvanaar.idea.Lua.lang.psi.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.stubs.LuaShortNamesCache;
import org.jetbrains.annotations.NotNull;


public class LuaPsiManager implements ProjectComponent
{
  private final Project myProject;
  private LuaShortNamesCache myCache;
  private PsiFile myDummyFile;

  public LuaPsiManager(Project project)
  {
    myProject = project;
  }

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }

  @NotNull
  public String getComponentName()
  {
    return "LuaPsiManager";
  }

  public void initComponent()
  {
    myCache = new LuaShortNamesCache(myProject);
    StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable()
    {
      public void run()
      {
        ApplicationManager.getApplication().runWriteAction(new Runnable()
        {
          public void run()
          {
            if (!myProject.isDisposed())
            {
              JavaPsiFacade.getInstance(myProject).registerShortNamesCache(getNamesCache());
            }
          }
        });
      }
    });

    myDummyFile =
      PsiFileFactory.getInstance(myProject)
        .createFileFromText("dummy." + LuaFileType.LUA_FILE_TYPE.getDefaultExtension(), "");
  }

  public void disposeComponent()
  {
  }

  public static LuaPsiManager getInstance(Project project)
  {
    return project.getComponent(LuaPsiManager.class);
  }

  public LuaShortNamesCache getNamesCache()
  {
    return myCache;
  }

}
