/*
 * Copyright 2009 Joachim Ansorg, mail@ansorg-it.com
 * File: LuaFacetUI.java, Class: LuaFacetUI
 * Last modified: 2010-02-17
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

package com.sylvanaar.idea.Lua.settings.facet.ui;

import com.sylvanaar.idea.Lua.settings.facet.LuaFacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashMap;

/**
 * GUI settings which are displayed for a Lua module facet.
 * <p/>
 * User: jansorg
 * Date: Feb 11, 2010
 * Time: 10:21:10 PM
 */
public class LuaFacetUI extends FacetEditorTab {
    private JRadioButton ignoreFilesWithoutExtensionRadioButton;
    private JRadioButton acceptAllFilesWithoutRadioButton;
    private JRadioButton customSettingsRadioButton;
    private JPanel basePanel;
    private JScrollPane treeScollArea;

    private ModuleFileTreeTable fileTreeTable;

    private final LuaFacetConfiguration facetConfiguration;
    private final FacetEditorContext facetEditorContext;

    public LuaFacetUI(LuaFacetConfiguration facetConfiguration, FacetEditorContext facetEditorContext, FacetValidatorsManager facetValidatorsManager) {
        this.facetConfiguration = facetConfiguration;
        this.facetEditorContext = facetEditorContext;
    }

    @Nls
    public String getDisplayName() {
        return "Lua Support";
    }

    private LuaFacetConfiguration.OperationMode findMode() {
        if (ignoreFilesWithoutExtensionRadioButton.isSelected()) {
            return LuaFacetConfiguration.OperationMode.IgnoreAll;
        }

        if (acceptAllFilesWithoutRadioButton.isSelected()) {
            return LuaFacetConfiguration.OperationMode.AcceptAll;
        }

        return LuaFacetConfiguration.OperationMode.Custom;
    }

    private void setMode(LuaFacetConfiguration.OperationMode mode) {
        acceptAllFilesWithoutRadioButton.setSelected(mode == LuaFacetConfiguration.OperationMode.AcceptAll);
        ignoreFilesWithoutExtensionRadioButton.setSelected(mode == LuaFacetConfiguration.OperationMode.IgnoreAll);
        customSettingsRadioButton.setSelected(mode == LuaFacetConfiguration.OperationMode.Custom);

        basePanel.setEnabled(customSettingsRadioButton.isSelected());
        fileTreeTable.setEnabled(customSettingsRadioButton.isSelected());
    }

    public JComponent createComponent() {
        customSettingsRadioButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JRadioButton button = (JRadioButton) e.getSource();
                treeScollArea.setEnabled(button.isSelected());

                basePanel.setEnabled(customSettingsRadioButton.isSelected());
                fileTreeTable.setEnabled(customSettingsRadioButton.isSelected());
            }
        });

        fileTreeTable = new ModuleFileTreeTable(facetEditorContext.getModule(), new HashMap<VirtualFile, FileMode>(facetConfiguration.getMapping()));
        treeScollArea.setViewportView(fileTreeTable);

        reset();

        return basePanel;
    }

    public boolean isModified() {
        return findMode() != facetConfiguration.getOperationMode() ||
                !fileTreeTable.getMapping().equals(facetConfiguration.getMapping());
    }

    public void apply() throws ConfigurationException {
        facetConfiguration.setOperationMode(findMode());
        facetConfiguration.setMapping(fileTreeTable.getMapping());
    }

    public void reset() {
        setMode(facetConfiguration.getOperationMode());
        fileTreeTable.reset(facetConfiguration.getMapping());
    }

    public void disposeUIResources() {
    }

    private void createUIComponents() {
    }
}
