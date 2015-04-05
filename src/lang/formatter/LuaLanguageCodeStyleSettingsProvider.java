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

package com.sylvanaar.idea.Lua.lang.formatter;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.sylvanaar.idea.Lua.LuaFileType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 19, 2010
 * Time: 7:37:11 PM
 */
public class LuaLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @NotNull
    @Override
    public Language getLanguage() {
        return LuaFileType.LUA_LANGUAGE;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer,
                                  @NotNull SettingsType settingsType) {
        List<String> settings = new ArrayList<String>();
        switch (settingsType) {
            case BLANK_LINES_SETTINGS:
                for (LuaSupportedCodeStyleSettings.BlankLinesOption blankLinesOption : LuaSupportedCodeStyleSettings
                        .BlankLinesOption.values()) {
                    settings.add(blankLinesOption.name());
                }
                break;
            case SPACING_SETTINGS:
                for (LuaSupportedCodeStyleSettings.SpacingOption spacingOption : LuaSupportedCodeStyleSettings
                        .SpacingOption.values()) {
                    settings.add(spacingOption.name());
                }
                break;
            case WRAPPING_AND_BRACES_SETTINGS:
                for (LuaSupportedCodeStyleSettings.WrappingOrBraceOption wrappingOrBraceOption :
                        LuaSupportedCodeStyleSettings
                        .WrappingOrBraceOption.values()) {
                    settings.add(wrappingOrBraceOption.name());
                }
                break;
            default:
                // ignore
                return;
        }

        consumer.showStandardOptions(settings.toArray(new String[settings.size()]));
    }

//    @Override
//    public PsiFile createFileFromText(Project project, String text) {
//        return LuaPsiElementFactory.getInstance(project).createLuaFile(text);
//    }



    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return "if b.state == 1 then\n" +
               "    if b.rotation then\n" +
               "    end\n" +
               "    \n" +
               "elseif b.state == 2 then\n" +
               "elseif b.state == 3 then\n" +
               "end\n" +
               "\n" +
               "\n" +
               "\n" +
               "local a = 1\n" +
               "local a = a\n" +
               "a = a\n" +
               "\n" +
               "print(\"hello\")\n" +
               "\n" +
               "a = {\n" +
               "    1, 2,\n" +
               "    3, 5\n" +
               "}\n" +
               "\n" +
               "if b.state == 1 then\n" +
               "    if b.rotation then\n" +
               "    end\n" +
               "elseif b.state == 2 then\n" +
               "elseif b.state == 3 then\n" +
               "end\n" +
               "\n" +
               "\n" +
               "function a.b.c() end\n" +
               "function a.b.d() end\n" +
               "function a.b.e() end\n" +
               "\n" +
               "\n" +
               "\n" +
               "bsdfasdssdfs = {\n" +
               "    a = 1,\n" +
               "    b = {\n" +
               "        v1 = 2\n" +
               "    },\n" +
               "    c = 3\n" +
               "}\n" +
               "\n" +
               "\n" +
               "myfas = {\n" +
               "    a = 1,\n" +
               "    a = 2,\n" +
               "    a = function() end,\n" +
               "    aasda = function()\n" +
               "                fsdf = 1\n" +
               "                s = 3\n" +
               "            end,\n" +
               "    a = function()\n" +
               "            a = 1\n" +
               "        end,\n" +
               "    b = function()\n" +
               "            a = 1\n" +
               "    \n" +
               "        end,\n" +
               "    a = 2,\n" +
               "}\n" +
               "\n" +
               "local function a(b, c, d) return b end\n" +
               "\n" +
               "local function b(c, d, e) return d() end\n" +
               "\n" +
               "function a:b(c, d, e) print(self) return self end\n" +
               "\n" +
               "function a.b(b, d, e)\n" +
               "    print(b)\n" +
               "    return b\n" +
               "end\n" +
               "\n" +
               "a.b(a, b, c)\n" +
               "\n" +
               "function a.b() end\n" +
               "function a:b() end\n" +
               "function aa.b() end\n" +
               "function aa:b()\n" +
               "\n" +
               "end\n" +
               "\n" +
               "function aa.b.c.d.e.f.d() end\n" +
               "\n" +
               "\n" +
               "print\"hello\"\n" +
               "\n" +
               "\n" +
               "function a()\n" +
               "end\n" +
               "\n" +
               "\n" +
               "\n" +
               "\n" +
               "\n" +
               "print(a, b, c)\n" +
               "\n" +
               "for c = b, c do\n" +
               "    for k, v in v do\n" +
               "        v = 2\n" +
               "        print(v)\n" +
               "        local function a()\n" +
               "        end\n" +
               "\n" +
               "        function a()\n" +
               "            print(v)\n" +
               "        end\n" +
               "    end\n" +
               "\n" +
               "    for v = 1, 2 do\n" +
               "        v = 2\n" +
               "        print(v)\n" +
               "        function a()\n" +
               "            print(v)\n" +
               "        end\n" +
               "    end\n" +
               "\n" +
               "    for k, v in v do\n" +
               "    end\n" +
               "end\n" +
               "\n" +
               "for k, v in v do\n" +
               "end";
    }


    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        CommonCodeStyleSettings settings = new CommonCodeStyleSettings(LuaFileType.LUA_LANGUAGE);
        settings.initIndentOptions();
        return settings;
    }
    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
      return new SmartIndentOptionsEditor();
    }
}
