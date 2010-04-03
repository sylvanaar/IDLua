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

package com.sylvanaar.idea.Lua.hilighter;

import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NonNls;



/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Apr 3, 2010
 * Time: 1:55:00 AM
 */
public class LuaHighlightingData {
    @NonNls
    static final String KEYWORD_ID = "LUA_KEYWORD";
    @NonNls
    static final String COMMENT_ID = "LUA_COMMENT";
    @NonNls
    static final String LONGCOMMENT_ID = "LUA_LONGCOMMENT";
    @NonNls
    static final String NUMBER_ID = "LUA_NUMBER";
    @NonNls
    static final String STRING_ID = "LUA_STRING";
    @NonNls
    static final String LONGSTRING_ID = "LUA_LONGSTRING";   
    @NonNls
    static final String LONGSTRING_BRACES_ID = "LUA_LONGSTRING_BRACES";
    @NonNls
    static final String LONGCOMMENT_BRACES_ID = "LUA_LONGCOMMENT_BRACES";
    @NonNls
    static final String BRACES_ID = "LUA_BRACES";
    @NonNls
    static final String PARENTHS_ID = "LUA_PARENTHS";
    @NonNls
    static final String BRACKETS_ID = "LUA_BRACKETS";
    @NonNls
    static final String BAD_CHARACTER_ID = "LUA_BAD_CHARACTER";
    @NonNls
    static final String IDENTIFIER_ID = "LUA_IDENTIFIER";
    @NonNls
    static final String VAR_ID = "LUA_VAR";
    @NonNls
    static final String COMMA_ID = "LUA_COMMA";
    @NonNls
    static final String SEMICOLON_ID = "LUA_SEMICOLON";
    @NonNls
    static final String SELF_ID = "LUA_SELF";


    public static final TextAttributesKey KEYWORD =
        TextAttributesKey.createTextAttributesKey(KEYWORD_ID, SyntaxHighlighterColors.KEYWORD.getDefaultAttributes().clone());
    public static final TextAttributesKey COMMENT =
        TextAttributesKey.createTextAttributesKey(COMMENT_ID, SyntaxHighlighterColors.LINE_COMMENT.getDefaultAttributes().clone());
    public static final TextAttributesKey LONGCOMMENT =
        TextAttributesKey.createTextAttributesKey(LONGCOMMENT_ID, SyntaxHighlighterColors.JAVA_BLOCK_COMMENT.getDefaultAttributes().clone());

    public static final TextAttributesKey LONGCOMMENT_BRACES =
        TextAttributesKey.createTextAttributesKey(LONGCOMMENT_BRACES_ID, SyntaxHighlighterColors.JAVA_BLOCK_COMMENT.getDefaultAttributes().clone());
    
    public static final TextAttributesKey NUMBER =
        TextAttributesKey.createTextAttributesKey(NUMBER_ID, SyntaxHighlighterColors.NUMBER.getDefaultAttributes().clone());
    public static final TextAttributesKey STRING =
        TextAttributesKey.createTextAttributesKey(STRING_ID, SyntaxHighlighterColors.STRING.getDefaultAttributes().clone());
    public static final TextAttributesKey LONGSTRING =
        TextAttributesKey.createTextAttributesKey(LONGSTRING_ID, SyntaxHighlighterColors.STRING.getDefaultAttributes().clone());
    public static final TextAttributesKey LONGSTRING_BRACES =
        TextAttributesKey.createTextAttributesKey(LONGSTRING_BRACES_ID, SyntaxHighlighterColors.STRING.getDefaultAttributes().clone());
    public static final TextAttributesKey BRACKETS =
        TextAttributesKey.createTextAttributesKey(BRACKETS_ID, SyntaxHighlighterColors.BRACKETS.getDefaultAttributes().clone());
    public static final TextAttributesKey BRACES =
        TextAttributesKey.createTextAttributesKey(BRACES_ID, SyntaxHighlighterColors.BRACES.getDefaultAttributes().clone());
        public static final TextAttributesKey PARENTHS =
        TextAttributesKey.createTextAttributesKey(PARENTHS_ID, SyntaxHighlighterColors.PARENTHS.getDefaultAttributes().clone());
    public static final TextAttributesKey BAD_CHARACTER =
        TextAttributesKey.createTextAttributesKey(BAD_CHARACTER_ID, HighlighterColors.BAD_CHARACTER.getDefaultAttributes().clone());



    public static final TextAttributesKey IDENTIFIER =
        TextAttributesKey.createTextAttributesKey(IDENTIFIER_ID, HighlighterColors.TEXT.getDefaultAttributes().clone());

    public static final TextAttributesKey SELF =
        TextAttributesKey.createTextAttributesKey(SELF_ID, SyntaxHighlighterColors.KEYWORD.getDefaultAttributes().clone());

    public static final TextAttributesKey COMMA =
        TextAttributesKey.createTextAttributesKey(COMMA_ID, SyntaxHighlighterColors.COMMA.getDefaultAttributes().clone());
    
    public static final TextAttributesKey SEMICOLON =
        TextAttributesKey.createTextAttributesKey(SEMICOLON_ID, SyntaxHighlighterColors.JAVA_SEMICOLON.getDefaultAttributes().clone());


    static {
    
    }
    
    
    
}
