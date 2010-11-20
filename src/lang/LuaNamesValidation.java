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

package com.sylvanaar.idea.Lua.lang;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import com.sylvanaar.idea.Lua.editor.completion.LuaKeywordsManager;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Aug 1, 2010
 * Time: 10:43:40 PM
 */
public class LuaNamesValidation implements NamesValidator {  
    public boolean isIdentifier(final String name, final Project project) {
        final int len = name.length();
        if (len == 0) return false;

//            ('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'_'|'0'..'9')*


        if (!Character.isLetter(name.charAt(0)) && name.charAt(0) != '_') return false;

        for (int i = 1; i < len; i++) {
            if (!Character.isLetter(name.charAt(i)) && name.charAt(i) != '_' && !Character.isDigit(i)) return false;
        }

        return true;
    }

    public boolean isKeyword(final String name, final Project project) {
        return LuaKeywordsManager.isKeywordName(name);
    }
}
