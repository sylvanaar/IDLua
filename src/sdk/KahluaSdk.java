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

package com.sylvanaar.idea.Lua.sdk;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Aug 28, 2010
 * Time: 11:43:09 AM
 */
public class KahluaSdk implements Sdk, ApplicationComponent {
    public static final String NAME = "Kahlua";
    
    private Sdk mySdk=null;

    public static KahluaSdk getInstance() {
       return ApplicationManager.getApplication().getComponent(KahluaSdk.class);
    }

    @Override
    public SdkType getSdkType() {
        return LuaSdkType.getInstance();
    }

    @Override
    public String getName() {
        return "Kahlua";
    }

    @Override
    public String getVersionString() {
        return "internal";
    }

    @Override
    public String getHomePath() {
        return "";
    }

    @Override
    public RootProvider getRootProvider() {
        return mySdk.getRootProvider();
    }

    @Override
    public SdkAdditionalData getSdkAdditionalData() {
        return null;
    }

    @Override
    public SdkModificator getSdkModificator() {
        return mySdk.getSdkModificator();
    }

    @Override
    public VirtualFile getHomeDirectory() {
        return mySdk.getHomeDirectory();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Kahlua Virtual SDK";
    }

    @Override
    public void initComponent() {
        ProjectJdkTable pjt = ProjectJdkTable.getInstance();
        mySdk = pjt.findJdk(KahluaSdk.NAME);

        if (mySdk == null) {
            mySdk = createMockSdk("",KahluaSdk.NAME);

            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    ProjectJdkTable.getInstance().addJdk(mySdk);
                }
            });
        }
    }

    @Override
    public void disposeComponent() {
       
    }


   private static Sdk createMockSdk(String jdkHome, final String versionName) {
    File jdkHomeFile = new File(jdkHome);
   // if (!jdkHomeFile.exists()) return null;

    final Sdk jdk = new ProjectJdkImpl(versionName, LuaSdkType.getInstance());
    final SdkModificator sdkModificator = jdk.getSdkModificator();

    String path = jdkHome.replace(File.separatorChar, '/');
    sdkModificator.setHomePath(path);
    sdkModificator.setVersionString(versionName); // must be set after home path, otherwise setting home path clears the version string

//    addSources(jdkHomeFile, sdkModificator);
//    addClasses(jdkHomeFile, sdkModificator, false);
//    addClasses(jdkHomeFile, sdkModificator, true);
    sdkModificator.commitChanges();

    return jdk;
  }

    //@Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

//    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
