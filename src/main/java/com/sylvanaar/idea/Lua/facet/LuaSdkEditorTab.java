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

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class LuaSdkEditorTab extends FacetEditorTab {
  private JPanel myMainPanel;
  private LuaSdkComboBox mySdkComboBox;
  private final FacetEditorContext myEditorContext;
  private final MessageBusConnection myConnection;

  public LuaSdkEditorTab(final FacetEditorContext editorContext) {
    myEditorContext = editorContext;
    final Project project = editorContext.getProject();
    mySdkComboBox.setProject(project);
    myConnection = project.getMessageBus().connect();
    myConnection.subscribe(ProjectJdkTable.JDK_TABLE_TOPIC, new ProjectJdkTable.Listener() {
      @Override
      public void jdkAdded(Sdk jdk) {
        mySdkComboBox.updateSdkList();
      }

      @Override
      public void jdkRemoved(Sdk jdk) {
        mySdkComboBox.updateSdkList();
      }

      @Override
      public void jdkNameChanged(Sdk jdk, String previousName) {
        mySdkComboBox.updateSdkList();
      }
    });
  }

  @Nls
  public String getDisplayName() {
    return "Lua SDK";
  }

  @NotNull
  public JComponent createComponent() {
    return myMainPanel;
  }

  public boolean isModified() {
    return mySdkComboBox.getSelectedSdk() != getFacetConfiguration().getSdk();
  }

  private LuaFacetConfiguration getFacetConfiguration() {
    return ((LuaFacetConfiguration) myEditorContext.getFacet().getConfiguration());
  }

  public void apply() {
    getFacetConfiguration().setSdk(mySdkComboBox.getSelectedSdk());
  }

  public void reset() {
    mySdkComboBox.updateSdkList(getFacetConfiguration().getSdk(), false);
  }

  public void disposeUIResources() {
    Disposer.dispose(myConnection);
  }

  public void setData(LuaSdkComboBox data) {
  }

  public void getData(LuaSdkComboBox data) {
  }

  public boolean isModified(LuaSdkComboBox data) {
    return false;
  }
}
