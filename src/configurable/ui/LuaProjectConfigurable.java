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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class LuaProjectConfigurable extends SearchableConfigurable.Parent.Abstract {
  private final Project project;
  private ProjectSettingsPanel optionsPanel = null;

  private static final Icon icon = IconLoader.getIcon("/resources/Lua32x32.png");

  private static final Logger logger = Logger.getInstance(LuaProjectConfigurable.class.getName());
  private final LuaProfilesPanel myProfilesPanel;


  public LuaProjectConfigurable(Project project) {
    this.project = project;
    myProfilesPanel = new LuaProfilesPanel(project);
  }

  public String getDisplayName() {
    return "Lua";
  }

  public Icon getIcon() {
    return icon;
  }

  public String getHelpTopic() {
    return getId();
  }

  public JComponent createComponent() {
    logger.info("createComponent()");
    optionsPanel = new ProjectSettingsPanel(project, myProfilesPanel);
    return optionsPanel.getMainComponent();
  }

  public boolean isModified() {
    logger.info("isModified()");
    boolean res = false;
    if (optionsPanel != null) {
      res = optionsPanel.isModified();
    }

    logger.info("isModified() = " + res);

    return res;
  }

  public void apply() throws ConfigurationException {
    logger.info("apply()");
    if (optionsPanel != null) {
      optionsPanel.apply();
    }
  }

  public void reset() {
    logger.info("reset()");
    if (optionsPanel != null) {
      optionsPanel.reset();
    }
  }

  public void disposeUIResources() {
    optionsPanel = null;
  }

  public boolean hasOwnContent() {
    return true;
  }

  public boolean isVisible() {
    return true;
  }

  public String getId() {
    return "Lua";
  }

  public Runnable enableSearch(String option) {
    return null;
  }

  protected Configurable[] buildConfigurables() {
    return new Configurable[]{myProfilesPanel, new LuaFormattingConfigurable(project)};
  }

}
