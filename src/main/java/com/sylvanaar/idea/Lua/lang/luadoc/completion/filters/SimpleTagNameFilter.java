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

package com.sylvanaar.idea.Lua.lang.luadoc.completion.filters;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.ElementFilter;
import com.sylvanaar.idea.Lua.lang.luadoc.lexer.LuaDocTokenTypes;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocTag;
import org.jetbrains.annotations.NonNls;

public class SimpleTagNameFilter implements ElementFilter {
    public boolean isAcceptable(Object element, PsiElement context) {
        if (context == null) return false;
        ASTNode node = context.getNode();
        return node != null && node.getElementType() == LuaDocTokenTypes.LDOC_TAG_NAME &&
               context.getParent() instanceof LuaDocTag;
    }

    public boolean isClassAcceptable(Class hintClass) {
        return true;
    }

    @NonNls
    public String toString() {
        return "Luadoc tagname filter";
    }
}