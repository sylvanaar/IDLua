/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.formatter;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.sylvanaar.idea.Lua.LuaFileType;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 10/1/11
 * Time: 12:51 PM
 */
public class LuaCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
  protected LuaCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(LuaFileType.LUA_LANGUAGE, currentSettings, settings);
  }

    @Override  // Turn off the other tabs for now
    protected void initTabs(CodeStyleSettings settings) {
        addIndentOptionsTab(settings);
    }
}
