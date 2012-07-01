/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.editor.completion;

import com.intellij.codeInsight.lookup.*;
import com.intellij.psi.*;
import com.sylvanaar.idea.Lua.*;
import org.jetbrains.annotations.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Nov 21, 2010
 * Time: 6:53:07 PM
 */
public class LuaCharFilter extends CharFilter {
    @Nullable
    public Result acceptChar(char c, int prefixLength, Lookup lookup) {
        if (!lookup.isCompletion()) return null;

        final PsiFile psiFile = lookup.getPsiFile();

        if (psiFile != null && !psiFile.getViewProvider().getLanguages().contains(LuaFileType.LUA_LANGUAGE))
            return null;

//        LookupElement item = lookup.getCurrentItem();
//        if (item == null) return null;

        if (Character.isJavaIdentifierPart(c)) return Result.ADD_TO_PREFIX;

        switch (c) {
            case ',':
            case ';':
            case '=':
            case '(':
            case '{':
                return Result.SELECT_ITEM_AND_FINISH_LOOKUP;

            case ':':
            case '.':
                return Result.ADD_TO_PREFIX;

            case '[':
            case ']':
                return Result.SELECT_ITEM_AND_FINISH_LOOKUP;

            default:
                return null;
        }
    }

}
