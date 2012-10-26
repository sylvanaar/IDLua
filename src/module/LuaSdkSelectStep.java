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
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.NotNullLazyValue;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class LuaSdkSelectStep extends ModuleWizardStep {
    private final LuaModuleBuilder myModuleBuilder;
    private final String           myHelp;
    private final Icon             myIcon;
    private       WizardContext    myContext;

    private NotNullLazyValue<LuaSdkChooserPanel> myPanel = new NotNullLazyValue<LuaSdkChooserPanel>() {
        @NotNull @Override
        protected LuaSdkChooserPanel compute() {
            return new LuaSdkChooserPanel(
                    myContext.getProject() == null ? ProjectManager.getInstance().getDefaultProject() : myContext
                            .getProject());
        }
    };

    public LuaSdkSelectStep(@NotNull final LuaModuleBuilder moduleBuilder, @Nullable final Icon icon,
                            @Nullable final String helpId, @NotNull final WizardContext context) {
        super();
        myIcon = icon;
        myModuleBuilder = moduleBuilder;
        myContext = context;
        myHelp = helpId;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myPanel.getValue().getPreferredFocusedComponent();
    }

    public JComponent getComponent() { return myPanel.getValue(); }

    public void updateDataModel() {
        final Sdk chosenJdk = myPanel.getValue().getChosenJdk();
        if (chosenJdk != null && chosenJdk.getSdkType().equals(LuaSdkType.getInstance())) {
            if (myContext.getProjectJdk() == null) {
                myContext.setProjectJdk(chosenJdk);
            } else {
                myModuleBuilder.setSdk(chosenJdk);
            }
        }
    }

    @Override
    public String getHelpId() { return myHelp; }

    @Override
    public boolean validate() { return true; }

    @Override
    public Icon getIcon() { return myIcon; }
}