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

package com.sylvanaar.idea.Lua.options;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiManager;
import com.sylvanaar.idea.Lua.LuaIcons;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 20, 2010
 * Time: 7:08:52 PM
 */
public class LuaOptionsPanel extends BaseConfigurable implements Configurable, ApplicationComponent {
    static final Logger log = Logger.getLogger(LuaOptionsPanel.class);

    private boolean modified = false;

    public LuaOptionsPanel() {
        addAdditionalCompletionsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModified(isModified(LuaApplicationSettings.getInstance()));
            }
        });
        resolveUpvaluedIdentifiersCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModified(isModified(LuaApplicationSettings.getInstance()));
            }
        });
    }

    JPanel getMainPanel() {
        return mainPanel;
    }

    private JPanel mainPanel;
    private JCheckBox addAdditionalCompletionsCheckBox;
    private JCheckBox resolveUpvaluedIdentifiersCheckBox;

    @Override
    public JComponent createComponent() {
        return getMainPanel();
    }

    public void apply() {
        getData(LuaApplicationSettings.getInstance());
        setModified(false);
    }

    public void reset() {
        setData(LuaApplicationSettings.getInstance());
    }

    @Override
    public void disposeUIResources() {

    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Lua";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Icon getIcon() {
        return LuaIcons.LUA_ICON;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Lua";
    }

    @Override
    public void initComponent() {
        setData(LuaApplicationSettings.getInstance());
    }

    @Override
    public void disposeComponent() {

    }

    public void setData(LuaApplicationSettings data) {
        addAdditionalCompletionsCheckBox.setSelected(data.INCLUDE_ALL_FIELDS_IN_COMPLETIONS);
        resolveUpvaluedIdentifiersCheckBox.setSelected(data.RESOLVE_ALIASED_IDENTIFIERS);

        if (data.RESOLVE_ALIASED_IDENTIFIERS) {
            for(Project project : ProjectManager.getInstance().getOpenProjects())
                PsiManager.getInstance(project).dropResolveCaches();
        }
    }

    public void getData(LuaApplicationSettings data) {
       data.INCLUDE_ALL_FIELDS_IN_COMPLETIONS = addAdditionalCompletionsCheckBox.isSelected();
       data.RESOLVE_ALIASED_IDENTIFIERS = resolveUpvaluedIdentifiersCheckBox.isSelected();
    }

    public boolean isModified(LuaApplicationSettings data) {
        if (addAdditionalCompletionsCheckBox.isSelected() != data.INCLUDE_ALL_FIELDS_IN_COMPLETIONS) return true;
        if (resolveUpvaluedIdentifiersCheckBox.isSelected() != data.RESOLVE_ALIASED_IDENTIFIERS) return true;
        return false;
    }
}
