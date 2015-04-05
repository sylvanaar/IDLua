///*
// * Copyright 2011 Jon S Akhtar (Sylvanaar)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package com.sylvanaar.idea.Lua.console;
//
//import com.intellij.openapi.actionSystem.AnAction;
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.openapi.actionSystem.LangDataKeys;
//import com.intellij.openapi.module.Module;
//import com.intellij.openapi.module.ModuleManager;
//import com.intellij.openapi.project.DumbAware;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.projectRoots.Sdk;
//import com.intellij.openapi.roots.ModuleRootManager;
//import com.sylvanaar.idea.Lua.LuaIcons;
//import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
//
///**
// * Created by IntelliJ IDEA.
// * User: Jon S Akhtar
// * Date: 2/20/11
// * Time: 5:26 PM
// */
//public class RunLuaConsoleAction extends AnAction implements DumbAware {
//
//    public RunLuaConsoleAction() {
//        getTemplatePresentation().setIcon(LuaIcons.LUA_ICON);
//    }
//
//    public void update(AnActionEvent e) {
//        e.getPresentation().setVisible(false);
//        e.getPresentation().setEnabled(false);
//        Project project = e.getData(LangDataKeys.PROJECT);
//        if (project != null) {
//            for (Module module : ModuleManager.getInstance(project).getModules()) {
//                e.getPresentation().setVisible(true);
//                Sdk luaSdk = LuaSdkType.findLuaSdk(module);
//                if (luaSdk != null) {
//                    final String homePath = luaSdk.getHomePath();
//                    if (homePath == null) {
//                        e.getPresentation().setEnabled(false);
//                        break;
//                    }
//                    if (LuaSdkType.getTopLevelExecutable(homePath).exists()) {
//                        e.getPresentation().setEnabled(true);
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    public void actionPerformed(AnActionEvent e) {
//        Project project = e.getData(LangDataKeys.PROJECT);
//
//        assert project != null;
//
//        Sdk sdk = null;
//        Module module = null;
//        Module modules[] = ModuleManager.getInstance(project).getModules();
//
//        for (Module m : modules) {
//            module = m;
//            sdk = LuaSdkType.findLuaSdk(m);
//            if (sdk != null) break;
//        }
//
//        assert module != null;
//        assert sdk != null;
//
//        String path = ModuleRootManager.getInstance(module).getContentRoots()[0].getPath();
//
//        LuaConsoleRunner.run(project, sdk, "Lua Console", path);
//    }
//
//
//}
