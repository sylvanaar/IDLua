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

package com.sylvanaar.idea.Lua.lang.parser;


import com.intellij.lang.*;
import com.intellij.lexer.*;
import com.intellij.openapi.project.*;
import com.intellij.psi.*;
import com.intellij.psi.tree.*;
import com.sylvanaar.idea.Lua.lang.lexer.*;
import com.sylvanaar.idea.Lua.lang.parser.kahlua.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.elements.*;
import org.jetbrains.annotations.*;

import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 04.07.2009
 * Time: 14:39:39
 */
public class LuaParserDefinition implements ParserDefinition {
    public static final IStubFileElementType LUA_FILE = new LuaStubFileElementType();
    //public static final IFileElementType LUA_FILE = new IFileElementType("Lua Script", LuaFileType.LUA_LANGUAGE);

    @NotNull
    public Lexer createLexer(Project project) {
        return new LuaParsingLexerMergingAdapter(new LuaLexer());
    }

    public PsiParser createParser(Project project) {
        return new KahluaParser();
    }

    public IFileElementType getFileNodeType() {
        return LUA_FILE;
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES_SET;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return COMMENT_SET;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return STRING_LITERAL_SET;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        final PsiElement element = LuaPsiCreator.createElement(node);

        return element;
    }


    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new LuaPsiFileImpl(fileViewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        if (left.getElementType() == LuaTokenTypes.SHORTCOMMENT) return SpaceRequirements.MUST_LINE_BREAK;

        if (left.getElementType() == LuaTokenTypes.NAME && KEYWORDS.contains(right.getElementType()))
            return SpaceRequirements.MUST;

        Lexer lexer = new LuaLexer();

        return LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer);
    }
}
