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

package com.sylvanaar.idea.Lua.run;

import com.intellij.execution.*;
import com.intellij.execution.actions.*;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.*;
import com.intellij.execution.junit.*;
import com.intellij.openapi.module.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.*;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.sylvanaar.idea.Lua.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

/**
 * This class is based on code of the intellij-batch plugin.
 *
 * @author wibotwi, jansorg, sylvanaar
 */
public class LuaRunConfigurationProducer extends RunConfigurationProducer<com.sylvanaar.idea.Lua.run.LuaRunConfiguration>  implements Cloneable {
    public LuaRunConfigurationProducer() {
        super(LuaConfigurationType.getInstance());
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull LuaRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        PsiFile sourceFile = Objects.requireNonNull(context.getLocation()).getPsiElement().getContainingFile();
        Location location = context.getLocation();
        if (sourceFile != null && sourceFile.getFileType().equals(LuaFileType.getFileType())) {
            VirtualFile file = sourceFile.getVirtualFile();

            if (file != null) {
                configuration.setName(file.getName());


                final VirtualFile dir = context.getProject().getBaseDir();
                if (dir != null) {
                    configuration.setWorkingDirectory(dir.getPath());

                    final String relativePath = FileUtil.getRelativePath(new File(dir.getPath()), new File(file
                            .getPath()));
                    configuration.setScriptName(StringUtil.notNullize(relativePath, "").replace('\\', '/'));
                } else
                    configuration.setScriptName(file.getPath());
            }

            com.intellij.openapi.module.Module module = ModuleUtil.findModuleForPsiElement(location.getPsiElement());
            if (module != null) {
                configuration.setModule(module);
            }

            if (StringUtil.isEmptyOrSpaces(configuration.getInterpreterPath())) {
                configuration.setOverrideSDKInterpreter(false);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull LuaRunConfiguration configuration, @NotNull ConfigurationContext context) {
        return false;
    }
}