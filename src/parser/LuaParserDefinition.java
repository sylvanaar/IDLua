/*
 * Copyright 2009 Max Ishchenko
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

package com.sylvanaar.idea.Lua.parser;


import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.lexer.LuaFlexLexer;
import com.sylvanaar.idea.Lua.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.psi.impl.LuaFileImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 04.07.2009
 * Time: 14:39:39
 */
public class LuaParserDefinition implements ParserDefinition {
    @NotNull
    public Lexer createLexer(Project project) {
        return new LuaFlexLexer();
    }

    public PsiParser createParser(Project project) {
        return new LuaParser(project);
    }

    public IFileElementType getFileNodeType() {
        return LuaTokenTypes.FILE;
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return LuaTokenTypes.WHITE_SPACES_SET;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return LuaTokenTypes.COMMENT_SET;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return LuaTokenTypes.STRING_LITERAL_SET;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
	    return LuaPsiCreator.createElement(node);
    }


    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new LuaFileImpl(fileViewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        //final Lexer lexer = createLexer(left.getPsi().getProject());
        return SpaceRequirements.MUST; //LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer);
    }
}
