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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
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

    public static Sdk findLuaSdk(Module module) {
        if (module == null) {
            return null;
        }

        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (sdk != null && (sdk.getSdkType().equals(LuaSdkType.getInstance()))) {
            return sdk;
        }

        return null;
    }

    @Override @NotNull
    public Icon getIconForAddAction() {
        return getIcon();
    }

    @Nullable
    public String suggestHomePath() {
        if (SystemInfo.isWindows) {
            return "C:\\Lua";
        } else if (SystemInfo.isLinux) {
            return "/usr/bin";
        }
        return null;
    }

    public boolean isValidSdkHome(@NotNull final String path) {
        final File lua = getTopLevelExecutable(path);
        // final File luac = getByteCodeCompilerExecutable(path);

        return lua.canExecute();// && luac.canExecute();
    }

    @NotNull
    public static File getTopLevelExecutable(@NotNull final String sdkHome) {
        File executable = getExecutable(sdkHome, "lua");
        if (executable.canExecute()) {
            return executable;
        }

        executable = getExecutable(sdkHome, "lua5.1");
        if (executable.canExecute()) {
            return executable;
        }

        executable = getExecutable(sdkHome, "luajit");
        if (executable.canExecute()) {
            return executable;
        }

        executable = getExecutable(sdkHome, "murgalua");

        return executable;
    }

    @NotNull
    public static File getByteCodeCompilerExecutable(@NotNull final String sdkHome) {
        File executable = getExecutable(sdkHome, "luac");
        if (executable.canExecute()) {
            return executable;
        }

        executable = getExecutable(sdkHome, "luac5.1");

        return executable;
    }

    @NotNull
    public String suggestSdkName(@Nullable final String currentSdkName, @NotNull final String sdkHome) {
        String[] version = getExecutableVersionOutput(sdkHome);
        if (version == null) {
            return "Unknown at " + sdkHome;
        }
        return version[0] + " " + version[1];
    }

    @Nullable
    public String getVersionString(@NotNull final String sdkHome) {
        return getExecutableVersionOutput(sdkHome)[1];
    }

    private String[] getExecutableVersionOutput(String sdkHome) {
        final String exePath = getTopLevelExecutable(sdkHome).getAbsolutePath();
        final ProcessOutput processOutput;
        try {
            processOutput = LuaSystemUtil.getProcessOutput(sdkHome, exePath, "-v");
        } catch (final ExecutionException e) {
            return null;
        }
        if (processOutput.getExitCode() != 0) {
            return null;
        }
        //Backwards compatibility - probably for Windows and OSX
        final String stderr = processOutput.getStderr().trim();
        if (!stderr.isEmpty()) {
            return stderr.split(" ");
        }
        //linux
        final String stdout = processOutput.getStdout().trim();
        if (!stdout.isEmpty()) {
            return stdout.split(" ");
        }
        return null;
    }

    @Nullable
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel,
                                                                       @NotNull final SdkModificator sdkModificator) {
        return null;
    }

    public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {
    }

    @NonNls
    public String getPresentableName() {
        return "Lua SDK";
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type) { return type == OrderRootType.CLASSES; }

    public void setupSdkPaths(@NotNull final Sdk sdk) {
        final SdkModificator[] sdkModificatorHolder = new SdkModificator[]{null};

        final SdkModificator sdkModificator = sdk.getSdkModificator();

        sdkModificator.addRoot(StdLibrary.getStdFileLocation(), OrderRootType.CLASSES);

        sdkModificatorHolder[0] = sdkModificator;

        if (sdkModificatorHolder[0] != null) {
            sdkModificatorHolder[0].commitChanges();
        }
    }

    @NotNull
    private static File getExecutable(@NotNull final String path, @NotNull final String command) {
        return new File(path, SystemInfo.isWindows ? command + ".exe" : command);
    }
}