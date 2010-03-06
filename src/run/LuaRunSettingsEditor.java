/*
 * Copyright 2009 Max Ishchenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ex.SingleConfigurableEditor;
import com.intellij.openapi.util.DimensionService;
import com.sylvanaar.idea.Lua.configurator.LuaConfigurationManager;
import com.sylvanaar.idea.Lua.configurator.LuaServerDescriptor;
import com.sylvanaar.idea.Lua.configurator.LuaServersConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 14.07.2009
 * Time: 19:29:20
 */
public class LuaRunSettingsEditor extends SettingsEditor<LuaRunConfiguration> {

    private LuaRunConfiguration config;
    private Mediator mediator;

    public LuaRunSettingsEditor(LuaRunConfiguration config) {
        this.config = config;
        this.mediator = new Mediator();
    }

    protected void applyEditorTo(LuaRunConfiguration s) throws ConfigurationException {
        mediator.applyEditorTo(s);
    }

    protected void resetEditorFrom(LuaRunConfiguration s) {
        mediator.resetEditorFrom(s);
    }

    @NotNull
    protected JComponent createEditor() {
        return new LuaRunSettingsForm(mediator).getPanel();
    }

    protected void disposeEditor() {
        mediator = null;
    }

    class Mediator {

        LuaRunSettingsForm form;

        void showServerManagerDialog() {
            //This is a little hack.
            //I could have used ShowSettingsUtil.editConfigurable(), but it gives me
            //little control over opening window. The main problem is dimensions.
            //The window will be too small on first open. So, i'm fixing dimensions on first open.
            //The dimension key generation logic is hidden in ShowSettingsUtil and I had to
            //copy key generation code here
            LuaConfigurationManager configManager = LuaConfigurationManager.getInstance();
            String dimensionServiceKey = "#" + configManager.getDisplayName().replaceAll("\n", "_").replaceAll(" ", "_");
            DimensionService dimensionService = DimensionService.getInstance();
            if (dimensionService.getSize(dimensionServiceKey) == null) {
                dimensionService.setSize(dimensionServiceKey, new Dimension(750, 500));
            }

            SingleConfigurableEditor editor = new SingleConfigurableEditor(form.panel, configManager, dimensionServiceKey);
            editor.show();

            resetEditorFrom(config);

        }

        public void applyEditorTo(LuaRunConfiguration s) {
            if (form.serverCombo.getSelectedItem() != null) {
                LuaServerDescriptor descriptor = (LuaServerDescriptor) form.serverCombo.getSelectedItem();
                s.setServerDescriptorId(descriptor.getId());
                s.setShowHttpLog(mediator.form.showHttpLogCheckBox.isSelected());
                s.setHttpLogPath(mediator.form.httpLogPathField.getText());
                s.setShowErrorLog(mediator.form.showErrorLogCheckBox.isSelected());
                s.setErrorLogPath(mediator.form.errorLogPathField.getText());
            }
        }

        public void onChooseDescriptor(LuaServerDescriptor descriptor) {
            if (descriptor != null) {
                form.executableField.setText(descriptor.getExecutablePath());
                form.configurationField.setText(descriptor.getConfigPath());
                form.pidField.setText(descriptor.getPidPath());
                form.globalsField.setText(descriptor.getGlobals());
                form.httpLogPathField.setText(descriptor.getHttpLogPath());
                form.errorLogPathField.setText(descriptor.getErrorLogPath());
            } else {
                form.executableField.setText("");
                form.configurationField.setText("");
                form.pidField.setText("");
                form.globalsField.setText("");
                form.httpLogPathField.setText("");
                form.errorLogPathField.setText("");
            }
        }

        public void onHttpLogCheckboxAction() {
            form.httpLogPathField.setEnabled(form.showHttpLogCheckBox.isSelected());
        }

        public void onErrorLogCheckboxAction() {
            form.errorLogPathField.setEnabled(form.showErrorLogCheckBox.isSelected());
        }

        public void resetEditorFrom(LuaRunConfiguration configuration) {
            DefaultComboBoxModel model = (DefaultComboBoxModel) form.serverCombo.getModel();
            model.removeAllElements();
            LuaServersConfiguration servers = LuaServersConfiguration.getInstance();
            for (LuaServerDescriptor descriptor : servers.getServersDescriptors()) {
                model.addElement(descriptor);
            }
            String chosenDescriptorId = configuration.getServerDescriptorId();
            if (chosenDescriptorId != null) {
                LuaServersConfiguration serversConfig = LuaServersConfiguration.getInstance();
                LuaServerDescriptor descriptor = serversConfig.getDescriptorById(chosenDescriptorId);
                if (descriptor != null) {
                    model.setSelectedItem(descriptor);
                } else {
                    model.setSelectedItem(null);
                }
            } else {
                model.setSelectedItem(null);
            }
            form.showHttpLogCheckBox.setSelected(configuration.isShowHttpLog());
            form.httpLogPathField.setEnabled(configuration.isShowHttpLog());
            form.showErrorLogCheckBox.setSelected(configuration.isShowErrorLog());
            form.errorLogPathField.setEnabled(configuration.isShowErrorLog());
        }

    }

}
