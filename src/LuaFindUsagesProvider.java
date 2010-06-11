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

package com.sylvanaar.idea.Lua;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.lexer.LuaFlexLexer;

import com.sylvanaar.idea.Lua.parser.LuaElementTypes;

import com.sylvanaar.idea.Lua.psi.LuaIdentifier;
import com.sylvanaar.idea.Lua.psi.impl.LuaIdentifierImpl;
import com.sylvanaar.idea.Lua.psi.statements.LuaFunctionDefinitionStatement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 27, 2010
 * Time: 8:27:20 PM
 */
public class LuaFindUsagesProvider implements FindUsagesProvider, LuaElementTypes {
    private static final class LuaWordsScanner extends DefaultWordsScanner {
        private static final TokenSet literals = TokenSet.orSet(LuaElementTypes.STRING_LITERAL_SET, TokenSet.create(NUMBER));

        public LuaWordsScanner() {
            super(new LuaFlexLexer(), FUNCTION_IDENTIFIER_SET, COMMENT_SET, literals);
            setMayHaveFileRefsInLiterals(true);
        }
    }

    public WordsScanner getWordsScanner() {
        return new LuaWordsScanner();
    }

    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    public String getType(@NotNull PsiElement element) {

        if (element instanceof LuaIdentifierImpl) {
            LuaIdentifier id = (LuaIdentifier) element;

            if (id.getContext() instanceof LuaFunctionDefinitionStatement)
                return "function";
            
            return "variable";
        }

        return "";
    }

    @NotNull
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (!canFindUsagesFor(element)) {
            return "";
        }

        String name = ((PsiNamedElement) element).getName();
        return name != null ? name : "";
    }

    @NotNull
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return getDescriptiveName(element);
    }
}