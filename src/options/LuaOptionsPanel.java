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
        enableIdentifierHilightingCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModified(isModified(LuaOptions.storedSettings()));
            }
        });
    }

    JPanel getMainPanel() {
        return mainPanel;
    }

    private JPanel mainPanel;
    private JCheckBox enableIdentifierHilightingCheckBox;

    @Override
    public JComponent createComponent() {
        return getMainPanel();
    }

    public void apply() {
        getData(LuaOptions.storedSettings());
        setModified(false);
    }

    public void reset() {
        setData(LuaOptions.storedSettings());
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
        setData(LuaOptions.storedSettings());
    }

    @Override
    public void disposeComponent() {

    }

    public void setData(LuaOptions data) {
        enableIdentifierHilightingCheckBox.setSelected(data.isIdentifierHilighting());
    }

    public void getData(LuaOptions data) {
        data.setIdentifierHilighting(enableIdentifierHilightingCheckBox.isSelected());
    }

    public boolean isModified(LuaOptions data) {
        if (enableIdentifierHilightingCheckBox.isSelected() != data.isIdentifierHilighting()) return true;
        return false;
    }
}
