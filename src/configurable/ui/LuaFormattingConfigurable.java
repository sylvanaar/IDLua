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

/*
 * User: anna
 * Date: 04-Dec-2008
 */
package com.sylvanaar.idea.Lua.configurable.ui;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.maddyhome.idea.Lua.util.FileTypeUtil;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.util.Arrays;

public class LuaFormattingConfigurable extends SearchableConfigurable.Parent.Abstract {
  private final Project myProject;
  private final TemplateCommentPanel myPanel;

  LuaFormattingConfigurable(Project project) {
    myProject = project;
    myPanel = new TemplateCommentPanel(null, null, null, project);
  }

  public String getId() {
    return "template.Lua.formatting";
  }

  public Runnable enableSearch(String option) {
    return null;
  }

  @Nls
    public String getDisplayName() {
    return "Formatting";
  }

  public Icon getIcon() {
    return null;
  }

  public String getHelpTopic() {
    return getId();
  }

  public JComponent createComponent() {
    return myPanel.createComponent();
  }

  public boolean isModified() {
    return myPanel.isModified();
  }

  public void apply() throws ConfigurationException {
    myPanel.apply();
  }

  public void reset() {
    myPanel.reset();
  }

  public void disposeUIResources() {
    myPanel.disposeUIResources();
  }

  public boolean hasOwnContent() {
    return true;
  }

  protected Configurable[] buildConfigurables() {
    final FileType[] types = FileTypeUtil.getInstance().getSupportedTypes();
    final Configurable[] children = new Configurable[types.length];
    Arrays.sort(types, new FileTypeUtil.SortByName());
    for (int i = 0; i < types.length; i++) {
      children[i] = FileTypeLuaConfigurableFactory.createFileTypeConfigurable(myProject, types[i], myPanel);
    }
    return children;
  }
}
