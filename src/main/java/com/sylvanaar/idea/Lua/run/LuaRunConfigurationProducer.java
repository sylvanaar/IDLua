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
import com.intellij.execution.impl.*;
import com.intellij.execution.junit.*;
import com.intellij.openapi.module.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.text.*;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.sylvanaar.idea.Lua.*;

/**
 * This class is based on code of the intellij-batch plugin.
 *
 * @author wibotwi, jansorg, sylvanaar
 */
public class LuaRunConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {
    private PsiFile sourceFile = null;

    public LuaRunConfigurationProducer() {
        super(LuaConfigurationType.getInstance());
    }

    @Override
    public PsiElement getSourceElement() {
        return sourceFile;
    }

    @Override
    protected RunnerAndConfigurationSettingsImpl createConfigurationByElement(Location location, ConfigurationContext configurationContext) {
        sourceFile = location.getPsiElement().getContainingFile();

        if (sourceFile != null && sourceFile.getFileType().equals(LuaFileType.LUA_FILE_TYPE)) {
            Project project = sourceFile.getProject();
            RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(project, configurationContext);

            VirtualFile file = sourceFile.getVirtualFile();

            LuaRunConfiguration runConfiguration = (LuaRunConfiguration) settings.getConfiguration();
            if (file != null) {
                runConfiguration.setName(file.getName());

                runConfiguration.setScriptName(file.getPath());
                final VirtualFile dir = configurationContext.getProject().getBaseDir();
                if (dir != null)
                    runConfiguration.setWorkingDirectory(dir.getPath());
            }

            Module module = ModuleUtil.findModuleForPsiElement(location.getPsiElement());
            if (module != null) {
                runConfiguration.setModule(module);
            }

            if (StringUtil.isEmptyOrSpaces(runConfiguration.getInterpreterPath())) {
                runConfiguration.setOverrideSDKInterpreter(false);
            }

            return (RunnerAndConfigurationSettingsImpl) settings;
        }

        return null;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}