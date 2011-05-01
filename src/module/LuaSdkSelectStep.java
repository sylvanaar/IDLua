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
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class LuaSdkSelectStep extends ModuleWizardStep {
    private final LuaModuleBuilder myModuleBuilder;
    private final LuaSdkChooserPanel myPanel;
    private final String myHelp;
    private final Icon myIcon;

    public LuaSdkSelectStep(@NotNull final LuaModuleBuilder moduleBuilder,
                              @Nullable final Icon icon,
                              @Nullable final String helpId,
                              @Nullable final Project project) {
        super();
        myIcon = icon;
        myModuleBuilder = moduleBuilder;
        myPanel = new LuaSdkChooserPanel(project);
        myHelp = helpId;
    }

    public String getHelpId() {
        return myHelp;
    }

    public JComponent getPreferredFocusedComponent() {
        return myPanel.getPreferredFocusedComponent();
    }

    public JComponent getComponent() {
        return myPanel;
    }

    public void updateDataModel() {
        myModuleBuilder.setSdk(myPanel.getChosenJdk());
    }

    public Icon getIcon() {
        return myIcon;
    }

    public boolean validate() {
        return true;
    }
}