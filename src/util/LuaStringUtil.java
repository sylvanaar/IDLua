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

package com.sylvanaar.idea.Lua.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 20.04.2010
 */
public class LuaStringUtil {
    @NotNull
    public static String getNotNull(@Nullable final String string) {
        return string == null ? "" : string;
    }

    @NotNull
    public static String firstLetterToUpperCase(@NotNull final String string) {
        if (string.length() == 0) return string;
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    @NotNull
    public static String firstLetterToLowerCase(@NotNull final String string) {
        if (string.length() == 0) return string;
        return Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }

    @NotNull
    public static String changeFirstLetterCase(final String string) {
        if (string.length() == 0) return string;
        return changeLetterCase(string.charAt(0)) + string.substring(1);
    }

    @NotNull
    public static String makeFirstLetterCaseTheSame(@NotNull final String string, @NotNull final String pattern) {
        return isUpperCase(pattern.charAt(0)) ? firstLetterToUpperCase(string) : firstLetterToLowerCase(string);
    }

    private static char changeLetterCase(final char letter) {
        return isUpperCase(letter) ? Character.toLowerCase(letter) : Character.toUpperCase(letter);
    }

    private static boolean isUpperCase(final char letter) {
        return letter == Character.toUpperCase(letter);
    }

    public static void insert(@NotNull final StringBuilder builder, final int position, @NotNull final String textToInsert) {
        if (position == builder.length()) {
            builder.append(textToInsert);
        }
        else {
            builder.insert(position, textToInsert);
        }
    }
}
