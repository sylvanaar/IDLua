/*
 * Copyright 2010 Joachim Ansorg, mail@ansorg-it.com
 * File: LuaProjectSettingsConfigurable.java, Class: LuaProjectSettingsConfigurable
 * Last modified: 2010-03-24
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.settings;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.sylvanaar.idea.Lua.LuaComponents;
import com.sylvanaar.idea.Lua.LuaIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Date: 12.05.2009
 * Time: 18:51:48
 *
 * @author Joachim Ansorg
 */
public class LuaProjectSettingsConfigurable implements ProjectComponent, Configurable {
    private LuaProjectSettingsPane settingsPanel;
    private final Project project;

    public LuaProjectSettingsConfigurable(Project project) {
        this.project = project;
    }

    @NotNull
    public String getComponentName() {
        return LuaComponents.LUA_LOADER + ".ProjectSettingsConfigurable";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
        if (settingsPanel != null) {
            this.settingsPanel.dispose();
            this.settingsPanel = null;
        }
    }

    @Nls
    public String getDisplayName() {
        return "LuaSupport";
    }

    public Icon getIcon() {
        return LuaIcons.LUA_ICON;
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        if (settingsPanel == null) {
            settingsPanel = new LuaProjectSettingsPane();
        }

        return settingsPanel.getPanel();
    }

    public boolean isModified() {
        if (settingsPanel == null) {
            return false;
        }

        return settingsPanel.isModified(LuaProjectSettings.storedSettings(project));
    }

    public void apply() throws ConfigurationException {
        settingsPanel.storeSettings(LuaProjectSettings.storedSettings(project));
    }

    public void reset() {
        settingsPanel.setData(LuaProjectSettings.storedSettings(project));
    }

    public void disposeUIResources() {
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }
}