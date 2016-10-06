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

import java.util.Arrays;
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

    public static final String AND   = "and";
    public static final String BREAK = "break";
    public static final String DO    = "do";
    public static final String ELSE  = "else";

    public static final String ELSEIF   = "elseif";
    public static final String END      = "end";
    public static final String FALSE    = "false";
    public static final String FOR      = "for";
    public static final String FUNCTION = "function";
    public static final String IF       = "if";
    public static final String IN       = "in";

    public static final String LOCAL  = "local";
    public static final String NIL    = "nil";
    public static final String NOT    = "not";
    public static final String OR     = "or";
    public static final String REPEAT = "repeat";
    public static final String RETURN = "return";
    public static final String THEN   = "then";

    public static final String TRUE  = "true";
    public static final String UNTIL = "until";
    public static final String WHILE = "while";

    public static Iterable<? extends String> getKeywords() {
        return keywords;
    }

    public static boolean isKeywordName(String name) {
        return keywords.contains(name);
    }

    static {
        keywords.add("and");
        keywords.add("break");
        keywords.add("do");
        keywords.add("else");

        keywords.add("elseif");
        keywords.add("end");
        keywords.add("false");
        keywords.add("for");
        keywords.add("function");
        keywords.add("if");
        keywords.add("in");

        keywords.add("local");
        keywords.add("nil");
        keywords.add("not");
        keywords.add("or");
        keywords.add("repeat");
        keywords.add("return");
        keywords.add("then");
        
        keywords.add("true");
        keywords.add("until");
        keywords.add("while");
    }
}
