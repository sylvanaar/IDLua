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

package com.sylvanaar.idea.Lua.configurable.ui;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.Configurable;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.options.LuaOptions;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.sylvanaar.idea.Lua.options.LuaOptionNames.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 20, 2010
 * Time: 7:08:52 PM
 */
public class LuaOptionsPanel extends BaseConfigurable implements Configurable, ApplicationComponent {
    static final Logger log = Logger.getLogger(LuaOptionsPanel.class);

    private JRadioButton radioButtonNone;

    private JRadioButton radioButtonLuaJ;

    private JRadioButton radioButtonLuaC;


    private JLabel lblLuaSyntaxCheck;


    private boolean modified = false;

    JPanel getMainPanel() {
        return mainPanel;
    }

    private final ActionListener SYNTAX_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (radioButtonLuaC.isSelected())
                    LuaOptions.getInstance().setValue(SYNTAX_CHECK_TYPE, SYNTAX_CHECK_TYPE_LUAC);
                if (radioButtonLuaJ.isSelected())
                    LuaOptions.getInstance().setValue(SYNTAX_CHECK_TYPE, SYNTAX_CHECK_TYPE_LUAJ);
                if (radioButtonNone.isSelected())
                    LuaOptions.getInstance().setValue(SYNTAX_CHECK_TYPE, SYNTAX_CHECK_TYPE_NONE);
            }
        };

    private JPanel mainPanel;

    @Override
    public JComponent createComponent() {
        radioButtonLuaC.addActionListener(SYNTAX_LISTENER);
        radioButtonNone.addActionListener(SYNTAX_LISTENER);
        radioButtonLuaJ.addActionListener(SYNTAX_LISTENER);

        String s = LuaOptions.getInstance().getValue(SYNTAX_CHECK_TYPE);

        if (s != null) {
            radioButtonLuaC.setSelected(s.equalsIgnoreCase(SYNTAX_CHECK_TYPE_LUAC));
            radioButtonNone.setSelected(s.equalsIgnoreCase(SYNTAX_CHECK_TYPE_NONE));
            radioButtonLuaJ.setSelected(s.equalsIgnoreCase(SYNTAX_CHECK_TYPE_LUAJ));
        }
        return getMainPanel();
    }

    public void apply() {
    }

    public void reset() {

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

    }

    @Override
    public void disposeComponent() {

    }
}
