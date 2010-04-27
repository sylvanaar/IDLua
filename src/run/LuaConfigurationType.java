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
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.sylvanaar.idea.Lua.LuaIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class LuaConfigurationType implements ConfigurationType {
    public String getDisplayName() {
        return "Lua";
    }

    public String getConfigurationTypeDescription() {
        return "Lua run configuration";
    }

    public Icon getIcon() {
        return LuaIcons.LUA_ICON;
    }

    @NotNull
    public String getId() {
        return "LuaConfigurationType";
    }

    public static LuaConfigurationType getInstance() {
        ConfigurationType[] configurationTypes = Extensions.getExtensions(CONFIGURATION_TYPE_EP);

        for (ConfigurationType configurationType : configurationTypes) {
            if (configurationType instanceof LuaConfigurationType) {
                return (LuaConfigurationType) configurationType;
            }
        }

        throw new IllegalStateException("Invalid state in getInstance");
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new LuaConfigurationFactory(this)};
    }

    private static class LuaConfigurationFactory extends ConfigurationFactory {
        public LuaConfigurationFactory(ConfigurationType batchConfigurationType) {
            super(batchConfigurationType);
        }

        @Override
        public RunConfiguration createTemplateConfiguration(Project project) {
         //   LuaInterpreterDetection LuaDetector = new LuaInterpreterDetection();

            LuaRunConfiguration configuration = new LuaRunConfiguration(new RunConfigurationModule(project), this, "");
           // configuration.setInterpreterPath(LuaDetector.findBestLocation());
            configuration.setInterpreterPath("lua");
            return configuration;
        }
    }
    
}