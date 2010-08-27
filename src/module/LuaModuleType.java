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

package com.sylvanaar.idea.Lua.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.sylvanaar.idea.Lua.LuaIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author Maxim.Manuylov
 *         Date: 23.03.2009
 */
public class LuaModuleType extends ModuleType<LuaModuleBuilder> {
    @NotNull
    public static final String ID = "LUA_MODULE";

    @NotNull
    public static LuaModuleType getInstance() {
        return (LuaModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(final WizardContext wizardContext,
                                                final LuaModuleBuilder moduleBuilder,
                                                final ModulesProvider modulesProvider) {
        final ArrayList<ModuleWizardStep> steps = new ArrayList<ModuleWizardStep>();
       // steps.add(new LuaSourcesPathStep(moduleBuilder, null, null));
        //steps.add(new LuaSdkSelectStep(moduleBuilder, null, null, wizardContext.getProject()));
        final ModuleWizardStep supportForFrameworksStep = ProjectWizardStepFactory.getInstance().createSupportForFrameworksStep(wizardContext, moduleBuilder);
        if (supportForFrameworksStep != null) {
            steps.add(supportForFrameworksStep);
        }
        return steps.toArray(new ModuleWizardStep[steps.size()]);
    }

    public LuaModuleType() {
        super(ID);
    }

    @NotNull
    public LuaModuleBuilder createModuleBuilder() {
        return new LuaModuleBuilder();
    }

    @NotNull
    public String getName() {
        return "Lua Module";
    }

    @NotNull
    public String getDescription() {
        return "Provides facilities for developing <strong>Lua</strong> applications.";
    }

    @NotNull
    public Icon getBigIcon() {
        return LuaIcons.LUA_IDEA_MODULE_ICON;
    }

    @NotNull
    public Icon getNodeIcon(final boolean isOpened) {
        return LuaIcons.LUA_ICON;
    }
}
