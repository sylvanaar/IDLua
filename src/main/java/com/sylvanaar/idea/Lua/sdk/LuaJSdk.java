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

package com.sylvanaar.idea.Lua.sdk;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.LuaBundle;
import com.sylvanaar.idea.Lua.util.LuaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Aug 28, 2010
 * Time: 11:43:09 AM
 */
public class LuaJSdk implements Sdk, ApplicationComponent {
    public static final String NAME = "LuaJ";

    private Sdk mySdk = null;

    public static LuaJSdk getInstance() {
        return ApplicationManager.getApplication().getComponent(LuaJSdk.class);
    }

    @NotNull
    @Override
    public SdkType getSdkType() {
        return LuaSdkType.getInstance();
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersionString() {
        return "2";
    }

    @Override
    public String getHomePath() {
        return "";
    }

    @NotNull
    @Override
    public RootProvider getRootProvider() {
        return mySdk.getRootProvider();
    }

    @Override
    public SdkAdditionalData getSdkAdditionalData() {
        return null;
    }

    @NotNull
    @Override
    public SdkModificator getSdkModificator() {
        return mySdk.getSdkModificator();
    }

    @Override
    public VirtualFile getHomeDirectory() {
        return mySdk.getHomeDirectory();
    }

    @NotNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return LuaBundle.message("luaj.componentname");
    }

    public String LUAJ_JAR = null;

    @Override
    public void initComponent() {
        final VirtualFile jarFile = getLuaJJarFile();
        if (jarFile != null) {
            LUAJ_JAR = LuaFileUtil.getPathToDisplay(jarFile);
        }

        ProjectJdkTable pjt = ProjectJdkTable.getInstance();
        mySdk = pjt.findJdk(LuaJSdk.NAME);

//        try {
//            if (Integer.parseInt(mySdk.getVersionString()) < 2) {
//                pjt.removeJdk(mySdk);
//                mySdk = null;
//            }
//        } catch (NumberFormatException e) {
//            pjt.removeJdk(mySdk);
//            mySdk = null;
//        }

        if (mySdk == null) {
            mySdk = createMockSdk("", LuaJSdk.NAME);

            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    ProjectJdkTable.getInstance().addJdk(mySdk);
                }
            });
        } else {
            final VirtualFile[] files = mySdk.getRootProvider().getFiles(OrderRootType.SOURCES);
            final VirtualFile stdRoot = StdLibrary.getStdFileLocation();
            final SdkModificator sdkModificator = mySdk.getSdkModificator();

            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    boolean found = false;
                    for (VirtualFile file : files)
                        if (file.equals(stdRoot)) {
                            found = true;
                        } else if (file.getName().contains(stdRoot.getName())) {
                            sdkModificator.removeRoot(file, OrderRootType.SOURCES);
                        }

                    if (!found)
                        sdkModificator.addRoot(stdRoot, OrderRootType.SOURCES);
                }
            });

            sdkModificator.commitChanges();
        }
    }

    @Nullable
    public static VirtualFile getLuaJJarFile() {
        final VirtualFile directory = LuaFileUtil.getPluginVirtualDirectory();
        if (directory != null) {
            final VirtualFile lib = directory.findChild("lib");
            if (lib != null) {
                VirtualFile[] children = lib.getChildren();
                if (children != null) {
                    for (VirtualFile child : children) {
                        if (child.getName().startsWith("luaj-jse")) {
                            return child;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void disposeComponent() {

    }


    public static Sdk createMockSdk(String jdkHome, final String versionName) {
        File jdkHomeFile = new File(jdkHome);
        // if (!jdkHomeFile.exists()) return null;

        final Sdk jdk = new ProjectJdkImpl(versionName, LuaSdkType.getInstance());
        final SdkModificator sdkModificator = jdk.getSdkModificator();

        String path = jdkHome.replace(File.separatorChar, '/');
        sdkModificator.setHomePath(path);
        sdkModificator.setVersionString(
                versionName); // must be set after home path, otherwise setting home path clears the version string
        sdkModificator.addRoot(StdLibrary.getStdFileLocation(), OrderRootType.SOURCES);
        sdkModificator.commitChanges();

        return jdk;
    }

    //@Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    //    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }
}
