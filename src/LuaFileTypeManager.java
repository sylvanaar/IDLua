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
//package com.sylvanaar.idea.Lua;
//
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.components.ApplicationComponent;
//import com.intellij.openapi.fileTypes.FileTypeManager;
//import com.sylvanaar.idea.Lua.configurator.LuaServersConfiguration;
//import org.jetbrains.annotations.NonNls;
//import org.jetbrains.annotations.NotNull;
//
///**
// * Created by IntelliJ IDEA.
// * User: Max
// * Date: 04.07.2009
// * Time: 1:02:46
// */
//public final class LuaFileTypeManager implements ApplicationComponent {
//
//    private LuaFileType fileType;
//
//    public static LuaFileTypeManager getInstance() {
//        return ApplicationManager.getApplication().getComponent(LuaFileTypeManager.class);
//    }
//
//    @NotNull
//    @NonNls
//    public String getComponentName() {
//        return "LuaFileTypeManager";
//    }
//
//    public void initComponent() {
//        fileType = new LuaFileType(LuaServersConfiguration.getInstance());
//        FileTypeManager.getInstance().registerFileType(fileType);
//    }
//
//    public void disposeComponent() {
//    }
//
//    public LuaFileType getFileType() {
//        return fileType;
//    }
//
//}
//
