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
//package com.sylvanaar.idea.Lua.options;
//
//import com.intellij.openapi.application.ApplicationManager;
//
//import java.io.Serializable;
//
//
//public class LuaOptions implements Serializable {
//    private boolean identifierHilighting = true;
//
//    public LuaOptions() {
//    }
//
//    public boolean isIdentifierHilighting() {
//        return identifierHilighting;
//    }
//
//    public void setIdentifierHilighting(final boolean identifierHilighting) {
//        this.identifierHilighting = identifierHilighting;
//    }
//
//    public static LuaOptions storedSettings() {
//        return ApplicationManager.getApplication().getComponent(LuaOptionsComponent.class).getState();
//    }
//}