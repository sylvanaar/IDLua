/*
 * Copyright 2015 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.injection;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.*;
import com.sylvanaar.idea.Lua.LuaFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jon on 12/12/2015.
 */
public class FrameXMLLanguageInjector implements MultiHostInjector {
    public static final String EMPTY_PREFIX = "local function __dummy(self) ";
    public static final String ON_ENTER_PREFIX = "local function __dummy (self,motion) ";
    public static final String ON_CHAR_PREFIX = "local function __dummy (self,text) ";
    public static final String ON_KEY_X_PREFIX = "local function __dummy (self,key) ";
    public static final String ON_DRAG_START_PREFIX = "local function __dummy (self,button) ";
    public static final String ON_MOUSE_WHEEL_PREFIX = "local function __dummy (self,delta) ";
    public static final String ON_MOUSE_X_PREFIX = "local function __dummy (self,button) ";
    public static final String ON_UPDATE_PREFIX = "local function __dummy (self,elapsed) ";
    public static final String ON_SIZE_CHANGED_PREFIX = "local function __dummy (self,w,h) ";
    public static final String ON_LEAVE_PREFIX = "local function __dummy (self,motion) ";
    public static final String ON_EVENT_PREFIX = "local function __dummy (self,event,...) ";
    public static final String ON_CLICK_PREFIX = "local function __dummy (self,button,down) ";
    public static final String ON_ATTRIBUTE_CHANGED_PREFIX = "local function __dummy (self,name,value) ";
    public static final String END_SUFFIX = " end";
    private Map<String, String> closureMap;


    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (context instanceof XmlText) {
            XmlTag tag = ((XmlText) context).getParentTag();
            if (tag == null) return;

            PsiFile file = tag.getContainingFile();
            if ((!(file instanceof XmlFile))) return;

            String tagName = tag.getLocalName();

            String prefix = getClosureMap().get(tagName.toLowerCase());

            if (prefix != null) {
                registrar.startInjecting(LuaFileType.LUA_LANGUAGE).addPlace(prefix, END_SUFFIX,
                        (PsiLanguageInjectionHost) context, new TextRange(0, context.getTextLength())).doneInjecting();
            }
        } else if (context instanceof XmlAttributeValue) {
            XmlAttribute value = (XmlAttribute) context.getParent();
            if (value == null || !value.getLocalName().equals("function")) return;

            XmlTag tag = value.getParent();
            if (tag == null) return;

            PsiFile file = tag.getContainingFile();
            if ((!(file instanceof XmlFile))) return;

            String tagName = tag.getLocalName();

            String prefix = getClosureMap().get(tagName.toLowerCase());

            if (prefix != null) {
                registrar.startInjecting(LuaFileType.LUA_LANGUAGE).addPlace("__DUMMY=", null,
                        (PsiLanguageInjectionHost) context, new TextRange(1, context.getTextLength()-1)).doneInjecting();
            }
        }
    }

    private Map<String, String> getClosureMap() {
        if (closureMap == null) {
            closureMap = new HashMap<String, String>();

            closureMap.put("onclick", ON_CLICK_PREFIX);
            closureMap.put("onevent", ON_EVENT_PREFIX);
            closureMap.put("onenter", ON_ENTER_PREFIX);
            closureMap.put("onleave", ON_LEAVE_PREFIX);
            closureMap.put("onload",  EMPTY_PREFIX);

            closureMap.put("onshow",  EMPTY_PREFIX);
            closureMap.put("onhide",  EMPTY_PREFIX);
            closureMap.put("ondragstop",  EMPTY_PREFIX);
            closureMap.put("onreceivedrag",  EMPTY_PREFIX);
            closureMap.put("onenable",  EMPTY_PREFIX);

            closureMap.put("ondisable",  EMPTY_PREFIX);
            closureMap.put("onkeyup", ON_KEY_X_PREFIX);
            closureMap.put("onkeydown", ON_KEY_X_PREFIX);
            closureMap.put("onchar",  ON_CHAR_PREFIX);
            closureMap.put("ondragstart",  ON_DRAG_START_PREFIX);

            closureMap.put("onmousewheel",  ON_MOUSE_WHEEL_PREFIX);
            closureMap.put("onmouseup",  ON_MOUSE_X_PREFIX);
            closureMap.put("onmousedown",  ON_MOUSE_X_PREFIX);
            closureMap.put("onattributechanged",  ON_ATTRIBUTE_CHANGED_PREFIX);
            closureMap.put("onupdate",  ON_UPDATE_PREFIX);

            closureMap.put("onsizechanged",  ON_SIZE_CHANGED_PREFIX);
        }

        return closureMap;
    }


    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Arrays.asList(XmlText.class, XmlAttributeValue.class);
    }
}
