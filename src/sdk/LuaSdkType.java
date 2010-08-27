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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.util.SystemInfo;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.util.LuaSystemUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

/**
 * @author Maxim.Manuylov
 *         Date: 03.04.2010
 */
public class LuaSdkType extends SdkType {
    @NotNull
    public static LuaSdkType getInstance() {
        return SdkType.findInstance(LuaSdkType.class);
    }

    public LuaSdkType() {
        super("Lua SDK");
    }

    @NotNull
    public Icon getIcon() {
        return LuaIcons.LUA_ICON;
    }

    @Override
    @NotNull
    public Icon getIconForAddAction() {
        return getIcon();
    }

    @Nullable
    public String suggestHomePath() {
        if (SystemInfo.isWindows) {
            return "C:\\cygwin\\bin";
        }
        else if (SystemInfo.isLinux) {
            return "/usr/bin";
        }
        return null;
    }

    public boolean isValidSdkHome(@NotNull final String path) {
        final File lua = getTopLevelExecutable(path);
        final File luac = getByteCodeCompilerExecutable(path);

        return lua.canExecute() && luac.canExecute();
    }

    @NotNull
    public static File getTopLevelExecutable(@NotNull final String sdkHome) {
        return getExecutable(sdkHome, "lua");
    }

    @NotNull
    public static File getByteCodeCompilerExecutable(@NotNull final String sdkHome) {
        return getExecutable(sdkHome, "luac");
    }

    @NotNull
    public String suggestSdkName(@Nullable final String currentSdkName, @NotNull final String sdkHome) {
        String version = getVersionString(sdkHome);
        if (version == null) return "Unknown at " + sdkHome;
        return "Lua " + version;
    }

    @Nullable
    public String getVersionString(@NotNull final String sdkHome) {
        final String exePath = getByteCodeCompilerExecutable(sdkHome).getAbsolutePath();
        final ProcessOutput processOutput;
        try {
            processOutput = LuaSystemUtil.getProcessOutput(sdkHome, exePath, "-v");
        } catch (final ExecutionException e) {
            return null;
        }
        if (processOutput.getExitCode() != 0) return null;
        final String stdout = processOutput.getStdout().trim();
        if (stdout.isEmpty()) return null;

        String[] sa = stdout.split(" ");

        return sa[1];
    }

    @Nullable
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel, @NotNull final SdkModificator sdkModificator) {
        return null;
    }

    public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {
    }

    @NonNls
    public String getPresentableName() {
        return "Lua SDK";
    }

    public void setupSdkPaths(@NotNull final Sdk sdk) {
//        final SdkModificator[] sdkModificatorHolder = new SdkModificator[] { null };
//        final ProgressManager progressManager = ProgressManager.getInstance();
//        final Project project = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
//        final Task.Modal setupTask = new Task.Modal(project, "Setting up library files", false) {
//            public void run(@NotNull final ProgressIndicator indicator) {
//                sdkModificatorHolder[0] = setupSdkPathsUnderProgress(sdk);
//            }
//        };
//        progressManager.run(setupTask);
//        if (sdkModificatorHolder[0] != null) sdkModificatorHolder[0].commitChanges();
    }

//    @NotNull
//    protected SdkModificator setupSdkPathsUnderProgress(@NotNull final Sdk sdk) {
//        final SdkModificator sdkModificator = sdk.getSdkModificator();
//        doSetupSdkPaths(sdkModificator);
//        return sdkModificator;
//    }
//
//    public void doSetupSdkPaths(@NotNull final SdkModificator sdkModificator) {
//        final String sdkHome = sdkModificator.getHomePath();
//
//        {
//            final File stdLibDir = new File(new File(new File(sdkHome).getParentFile(), "lib"), "lua");
//            if (tryToProcessAsStandardLibraryDir(sdkModificator, stdLibDir)) return;
//        }
//
//        try {
//            final String exePath = getByteCodeCompilerExecutable(sdkHome).getAbsolutePath();
//            final ProcessOutput processOutput = LuaSystemUtil.getProcessOutput(sdkHome, exePath, "-where");
//            if (processOutput.getExitCode() == 0) {
//                final String stdout = processOutput.getStdout().trim();
//                if (!stdout.isEmpty()) {
//                    if (SystemInfo.isWindows && stdout.startsWith("/")) {
//                        for (final File root : File.listRoots()) {
//                            final File stdLibDir = new File(root, stdout);
//                            if (tryToProcessAsStandardLibraryDir(sdkModificator, stdLibDir)) return;
//                        }
//                    }
//                    else {
//                        final File stdLibDir = new File(stdout);
//                        if (tryToProcessAsStandardLibraryDir(sdkModificator, stdLibDir)) return;
//                    }
//                }
//            }
//        }
//        catch (final ExecutionException ignore) {}
//
//        final File stdLibDir = new File("/usr/lib/lua");
//        tryToProcessAsStandardLibraryDir(sdkModificator, stdLibDir);
//    }
//
//    private boolean tryToProcessAsStandardLibraryDir(@NotNull final SdkModificator sdkModificator, @NotNull final File stdLibDir) {
//        if (!isStandardLibraryDir(stdLibDir)) return false;
//        final VirtualFile dir = LocalFileSystem.getInstance().findFileByIoFile(stdLibDir);
//        if (dir != null) {
//            sdkModificator.addRoot(dir, OrderRootType.SOURCES);
//            sdkModificator.addRoot(dir, OrderRootType.CLASSES);
//        }
//        return true;
//    }
//
//    private boolean isStandardLibraryDir(@NotNull final File dir) {
//        if (!dir.isDirectory()) return false;
//        final File pervasives_ml = new File(dir, "pervasives.ml");
//        final File pervasives_mli = new File(dir, "pervasives.mli");
//        final File pervasives_cmi = new File(dir, "pervasives.cmi");
//        final File pervasives_cmx = new File(dir, "pervasives.cmx");
//        return pervasives_ml.isFile() && pervasives_mli.isFile() && pervasives_cmi.isFile() && pervasives_cmx.isFile();
//    }

    @NotNull
    private static File getExecutable(@NotNull final String path, @NotNull final String command) {
        return new File(path, SystemInfo.isWindows ? command + ".exe" : command);
    }
}