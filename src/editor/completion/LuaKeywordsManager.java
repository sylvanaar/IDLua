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

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 16, 2010
 * Time: 10:43:29 AM
 */
public class LuaKeywordsManager {
    private static final Set<String> keywords = new HashSet<String>();

    public Iterable<? extends String> getKeywords() {
        return keywords;
    }

    static {
        keywords.add("function");
        keywords.add("if");
        keywords.add("then");
        keywords.add("else");
        keywords.add("elseif");
        keywords.add("for");
        keywords.add("do");
        keywords.add("while");
        keywords.add("end");
        keywords.add("repeat");
    }
}
