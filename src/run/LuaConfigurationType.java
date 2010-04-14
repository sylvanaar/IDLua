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

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.sylvanaar.idea.Lua.LuaBundle;
import com.sylvanaar.idea.Lua.LuaIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class LuaConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myFactory;
  private String myVmParameters;

  LuaConfigurationType() {
      
    myFactory = new ConfigurationFactory(this) {
        @Override
        public RunConfiguration createTemplateConfiguration(Project project) {
            return new LuaRunConfiguration(project, this, "");
        }
    };
  }
  public String getDisplayName() {
    return LuaBundle.message("run.configuration.title");
  }

  public String getConfigurationTypeDescription() {
    return LuaBundle.message("run.configuration.type.description");
  }

  public Icon getIcon() {
    return LuaIcons.LUA_ICON;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[] {myFactory};
  }

  @NotNull
  public String getId() {
    return "#com.sylvanaar.idea.Lua.run.LuaConfigurationType";
  }

 
}