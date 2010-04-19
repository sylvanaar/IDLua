/*
 * Lua 2010 Jon S Akhtar (Sylvanaar)
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

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.Comparing;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LuaConfigurable extends NamedConfigurable<LuaProfile> {
  private final LuaProfile myLuaProfile;
  private JPanel myWholePanel;

  private final Project myProject;
  private boolean myModified;

  private String myDisplayName;
  private JEditorPane myLuaPane;
  private JButton myValidateButton;
  private JTextField myKeywordTf;
  private JTextField myAllowReplaceTextField;

  public LuaConfigurable(Project project, LuaProfile LuaProfile, Runnable updater) {
    super(true, updater);
    myProject = project;
    myLuaProfile = LuaProfile;
    myDisplayName = myLuaProfile.getName();
  }

  public void setDisplayName(String s) {
    myLuaProfile.setName(s);
  }

  public LuaProfile getEditableObject() {
    return myLuaProfile;
  }

  public String getBannerSlogan() {
    return myLuaProfile.getName();
  }

  public JComponent createOptionsPanel() {
    myValidateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          VelocityHelper.verify(myLuaPane.getText());
          Messages.showInfoMessage(myProject, "Velocity template valid.", "Validation");
        }
        catch (Exception e1) {
          Messages.showInfoMessage(myProject, "Velocity template error:\n" + e1.getMessage(), "Validation");
        }
      }
    });
    return myWholePanel;
  }

  @Nls
  public String getDisplayName() {
    return myLuaProfile.getName();
  }

  @Nullable
  public Icon getIcon() {
    return null;
  }

  @Nullable
  @NonNls
  public String getHelpTopic() {
    return null;
  }

  public boolean isModified() {
    return myModified ||
           !Comparing.strEqual(EntityUtil.encode(myLuaPane.getText().trim()), myLuaProfile.getNotice()) ||
           !Comparing.strEqual(myKeywordTf.getText().trim(), myLuaProfile.getKeyword()) ||
           !Comparing.strEqual(myAllowReplaceTextField.getText().trim(), myLuaProfile.getAllowReplaceKeyword()) ||
           !Comparing.strEqual(myDisplayName, myLuaProfile.getName());
  }

  public void apply() throws ConfigurationException {
    myLuaProfile.setNotice(EntityUtil.encode(myLuaPane.getText().trim()));
    myLuaProfile.setKeyword(myKeywordTf.getText());
    myLuaProfile.setAllowReplaceKeyword(myAllowReplaceTextField.getText());
    LuaManager.getInstance(myProject).addLua(myLuaProfile);
    myDisplayName = myLuaProfile.getName();
    myModified = false;
  }

  public void reset() {
    myDisplayName = myLuaProfile.getName();
    myLuaPane.setText(EntityUtil.decode(myLuaProfile.getNotice()));
    myKeywordTf.setText(myLuaProfile.getKeyword());
    myAllowReplaceTextField.setText(myLuaProfile.getAllowReplaceKeyword());
  }

  public void disposeUIResources() {
  }

  public void setModified(boolean modified) {
    myModified = modified;
  }
}
