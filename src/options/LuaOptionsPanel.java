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

import com.intellij.codeInsight.daemon.*;
import com.intellij.openapi.options.*;
import com.intellij.openapi.project.*;
import com.intellij.psi.*;
import com.sylvanaar.idea.Lua.*;
import org.apache.log4j.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 20, 2010
 * Time: 7:08:52 PM
 */
public class LuaOptionsPanel extends BaseConfigurable implements Configurable {
    static final Logger log = Logger.getLogger(LuaOptionsPanel.class);

    public LuaOptionsPanel() {
        addAdditionalCompletionsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModified(isModified(LuaApplicationSettings.getInstance()));
            }
        });
        enableTypeInference.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModified(isModified(LuaApplicationSettings.getInstance()));
            }
        });
        checkBoxTailCalls.addActionListener(new ActionListener() {
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
    private JCheckBox enableTypeInference;
    private JCheckBox checkBoxTailCalls;

    @Override
    public JComponent createComponent() {
        setData(LuaApplicationSettings.getInstance());
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
        return LuaFileType.LUA;
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    public void setData(LuaApplicationSettings data) {
        addAdditionalCompletionsCheckBox.setSelected(data.INCLUDE_ALL_FIELDS_IN_COMPLETIONS);
        enableTypeInference.setSelected(data.ENABLE_TYPE_INFERENCE);
        checkBoxTailCalls.setSelected(data.SHOW_TAIL_CALLS_IN_GUTTER);
    }

    public void getData(LuaApplicationSettings data) {
        if (checkBoxTailCalls.isSelected() != data.SHOW_TAIL_CALLS_IN_GUTTER) {
            data.SHOW_TAIL_CALLS_IN_GUTTER = checkBoxTailCalls.isSelected();

            for(Project project : ProjectManager.getInstance().getOpenProjects())
              DaemonCodeAnalyzer.getInstance(project).restart();
        }

        data.INCLUDE_ALL_FIELDS_IN_COMPLETIONS = addAdditionalCompletionsCheckBox.isSelected();
        data.ENABLE_TYPE_INFERENCE = enableTypeInference.isSelected();
        if (data.ENABLE_TYPE_INFERENCE) {
            for (Project project : ProjectManager.getInstance().getOpenProjects())
                PsiManager.getInstance(project).dropResolveCaches();
        }
    }

    public boolean isModified(LuaApplicationSettings data) {
        if (addAdditionalCompletionsCheckBox.isSelected() != data.INCLUDE_ALL_FIELDS_IN_COMPLETIONS) return true;
        if (enableTypeInference.isSelected() != data.ENABLE_TYPE_INFERENCE) return true;
        if (checkBoxTailCalls.isSelected() != data.SHOW_TAIL_CALLS_IN_GUTTER) return true;

        return false;
    }
}
