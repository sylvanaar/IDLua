///*
// * Copyright 2009 Max Ishchenko
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.sylvanaar.idea.Lua.run;
//
//import com.intellij.execution.configurations.ConfigurationFactory;
//import com.intellij.execution.configurations.ConfigurationType;
//import com.intellij.execution.configurations.RunConfiguration;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.util.IconLoader;
//import com.sylvanaar.idea.Lua.LuaBundle;
//import org.jetbrains.annotations.NotNull;
//
//import javax.swing.*;
//
///**
// * Created by IntelliJ IDEA.
// * User: Max
// * Date: 14.07.2009
// * Time: 19:10:29
// */
//public class LuaConfigurationType implements ConfigurationType {
//
//    LuaConfigurationFactory ncf = new LuaConfigurationFactory(this);
//
//    public String getDisplayName() {
//        return LuaBundle.message("cofigurationtype.displayname");
//    }
//
//    public String getConfigurationTypeDescription() {
//        return LuaBundle.message("configurationtype.description");
//    }
//
//    public Icon getIcon() {
//        return IconLoader.getIcon("/net/ishchenko/idea/Lua/Lua.png");
//    }
//
//    @NotNull
//    public String getId() {
//        return "Lua.configuration.type";
//    }
//
//    public ConfigurationFactory[] getConfigurationFactories() {
//        return new ConfigurationFactory[]{ncf};
//    }
//
//    private static class LuaConfigurationFactory extends ConfigurationFactory {
//
//        protected LuaConfigurationFactory(@NotNull ConfigurationType type) {
//            super(type);
//        }
//
//        public RunConfiguration createTemplateConfiguration(Project project) {
//            return new LuaRunConfiguration(project, this, LuaBundle.message("cofigurationtype.displayname"));
//        }
//    }
//}
