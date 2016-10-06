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

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * User: jansorg
 * Date: 10.07.2009
 * Time: 21:43:12
 */
public class LuaCommonOptionsForm implements CommonLuaRunConfigurationParams {
    private JPanel rootPanel;
    private RawCommandLineEditor interpreterOptions;
    private EnvironmentVariablesComponent environmentVariablesEdit;
    private TextFieldWithBrowseButton luaInterpreterEdit;
    private TextFieldWithBrowseButton workingDirEdit;
    private JRadioButton kahluaRadioButton;
    private JRadioButton luajRadioButton;
    private JCheckBox useSDKCheckbox;

    public LuaCommonOptionsForm(LuaRunConfiguration luaRunConfiguration) {
        luaInterpreterEdit.addBrowseFolderListener("Select Lua Interpreter", "", luaRunConfiguration.getProject(), BrowseFilesListener.SINGLE_FILE_DESCRIPTOR);
        workingDirEdit.addBrowseFolderListener("Select Working Directory", "", luaRunConfiguration.getProject(), BrowseFilesListener.SINGLE_DIRECTORY_DESCRIPTOR);

        useSDKCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateInterpreterOptionsWidgets();
            }
        });
    }

    private void updateInterpreterOptionsWidgets() {
        luaInterpreterEdit.setEnabled(!useSDKCheckbox.isSelected());
    }

    @Override
    public String getInterpreterOptions() {
        return interpreterOptions.getText();
    }

    @Override
    public void setInterpreterOptions(String options) {
        interpreterOptions.setText(options);
    }

    @Override
    public String getWorkingDirectory() {
        return workingDirEdit.getText();
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        workingDirEdit.setText(workingDirectory);
    }

    @Override
    public Map<String, String> getEnvs() {
        return environmentVariablesEdit.getEnvs();
    }

    @Override
    public void setEnvs(Map<String, String> envs) {
        environmentVariablesEdit.setEnvs(envs);
    }

    @Override
    public String getInterpreterPath() {
        return luaInterpreterEdit.getText();
    }

    @Override
    public void setInterpreterPath(String path) {
        this.luaInterpreterEdit.setText(path);
    }

    @Override
    public boolean isOverrideSDKInterpreter() {
        return !useSDKCheckbox.isSelected();
    }

    @Override
    public void setOverrideSDKInterpreter(boolean b) {
        useSDKCheckbox.setSelected(!b);
        updateInterpreterOptionsWidgets();
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here 
    }
}
