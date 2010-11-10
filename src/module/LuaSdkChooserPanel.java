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

import com.intellij.ide.util.projectWizard.JdkChooserPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.MultiLineLabelUI;
import com.intellij.util.ui.UIUtil;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Maxim.Manuylov
 *         Date: 03.04.2010
 */
class LuaSdkChooserPanel extends JComponent {
    @NotNull private final JdkChooserPanel myJdkChooser;

    public LuaSdkChooserPanel(final Project project) {
        myJdkChooser = new JdkChooserPanel(project);

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder());

        final JLabel label = new JLabel("Specify the Lua binaries directory");
        label.setUI(new MultiLineLabelUI());
        add(label, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, new Insets(8, 10, 8, 10), 0, 0));

        final JLabel jdkLabel = new JLabel("Lua SDK version:");
        jdkLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
        add(jdkLabel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE, new Insets(8, 10, 0, 10), 0, 0));

        add(myJdkChooser, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.BOTH, new Insets(2, 10, 10, 5), 0, 0));
        JButton configureButton = new JButton("Configure...");
        add(configureButton, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE, new Insets(2, 0, 10, 5), 0, 0));

        configureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myJdkChooser.editJdkTable();
            }
        });

        myJdkChooser.setAllowedJdkTypes(new SdkType[] { LuaSdkType.getInstance() });

// IDEAX        final Sdk selectedJdk = project == null ? null : ProjectRootManager.getInstance(project).getProjectSdk();
        final Sdk selectedJdk = project == null ? null : ProjectRootManager.getInstance(project).getProjectJdk();
        myJdkChooser.updateList(selectedJdk, null);
    }

    @Nullable
    public Sdk getChosenJdk() {
        return myJdkChooser.getChosenJdk();
    }

    public JComponent getPreferredFocusedComponent() {
        return myJdkChooser;
    }

    public void selectSdk(@Nullable final Sdk sdk) {
        myJdkChooser.selectJdk(sdk);
    }
}