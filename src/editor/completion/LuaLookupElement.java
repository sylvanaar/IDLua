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

package com.sylvanaar.idea.Lua.editor.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 16, 2010
 * Time: 10:50:28 AM
 */
public class LuaLookupElement extends LookupElement {
    private String str;
    boolean isSelf = false;

    public LuaLookupElement(String str) {
        this.str = str;
    }

    public LuaLookupElement(String str, boolean isSelf) {
        this.str = str;
        this.isSelf = isSelf;
    }

    @NotNull
    public String getLookupString() {
        if (!isSelf)
            return str;

        int colonIdx = str.indexOf(':');

        assert colonIdx > 0;

        String suffix = str.substring(colonIdx);

        return "self" + suffix;
    }
}



