/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package com.sylvanaar.idea.Lua.facet;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;

import javax.swing.*;
import java.util.List;

/**
 * @author yole
 */
public class LuaSdkComboBox extends ComboboxWithBrowseButton {
  private Project myProject;

  LuaSdkComboBox() {
    addActionListener(e -> {
      Sdk selectedSdk = getSelectedSdk();
      final Project project = myProject != null ? myProject : ProjectManager.getInstance().getDefaultProject();
      ProjectJdksEditor editor = new ProjectJdksEditor(selectedSdk, project, LuaSdkComboBox.this);
      if (editor.showAndGet()) {
        selectedSdk = editor.getSelectedJdk();
        updateSdkList(selectedSdk, false);
      }
    });
    updateSdkList(null, true);
  }

  public void setProject(Project project) {
    myProject = project;
  }

  void updateSdkList(Sdk sdkToSelect, boolean selectAnySdk) {
    final List<Sdk> sdkList = ProjectJdkTable.getInstance().getSdksOfType(LuaSdkType.getInstance());
    if (selectAnySdk && sdkList.size() > 0) {
      sdkToSelect = sdkList.get(0);
    }
    sdkList.add(0, null);
    getComboBox().setModel(new DefaultComboBoxModel(sdkList.toArray(new Sdk[sdkList.size()])));
    getComboBox().setSelectedItem(sdkToSelect);
  }

  public void updateSdkList() {
    updateSdkList((Sdk) getComboBox().getSelectedItem(), false);
  }

  Sdk getSelectedSdk() {
    return (Sdk) getComboBox().getSelectedItem();
  }
}
