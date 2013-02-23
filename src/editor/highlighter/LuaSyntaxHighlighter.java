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

package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.lexer.LuaLexer;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.LuaDocElementTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 06.07.2009
 * Time: 16:40:05
 */
public class LuaSyntaxHighlighter extends SyntaxHighlighterBase {

    private final TextAttributesKey[] BAD_CHARACTER_KEYS = new TextAttributesKey[]{HighlighterColors.BAD_CHARACTER};

    private final Map<IElementType, TextAttributesKey> colors = new HashMap<IElementType, TextAttributesKey>();

    public LuaSyntaxHighlighter() {
        colors.put(LuaTokenTypes.LONGCOMMENT, LuaHighlightingData.LONGCOMMENT);
        colors.put(LuaTokenTypes.LONGCOMMENT_BEGIN, LuaHighlightingData.LONGCOMMENT_BRACES);
        colors.put(LuaTokenTypes.LONGCOMMENT_END, LuaHighlightingData.LONGCOMMENT_BRACES);
        colors.put(LuaTokenTypes.SHORTCOMMENT, LuaHighlightingData.COMMENT);
        colors.put(LuaTokenTypes.SHEBANG, LuaHighlightingData.COMMENT);

        colors.put(LuaTokenTypes.STRING, LuaHighlightingData.STRING);
        colors.put(LuaTokenTypes.LONGSTRING, LuaHighlightingData.LONGSTRING);
        colors.put(LuaTokenTypes.LONGSTRING_BEGIN, LuaHighlightingData.LONGSTRING_BRACES);
        colors.put(LuaTokenTypes.LONGSTRING_END, LuaHighlightingData.LONGSTRING_BRACES);

        fillMap(colors, LuaTokenTypes.OPERATORS_SET, LuaHighlightingData.OPERATORS);
        fillMap(colors, KEYWORDS, LuaHighlightingData.KEYWORD);
        fillMap(colors, PARENS, LuaHighlightingData.PARENTHESES);
        fillMap(colors, BRACES, LuaHighlightingData.BRACES);
        fillMap(colors, BRACKS, LuaHighlightingData.BRACKETS);

        colors.put(SEMI, LuaHighlightingData.SEMI);

        fillMap(colors, BAD_INPUT, LuaHighlightingData.BAD_CHARACTER);
        fillMap(colors, DEFINED_CONSTANTS, LuaHighlightingData.DEFINED_CONSTANTS);
        colors.put(LuaTokenTypes.COMMA, LuaHighlightingData.COMMA);
        colors.put(LuaTokenTypes.NUMBER, LuaHighlightingData.NUMBER);

        colors.put(LuaDocElementTypes.LUADOC_COMMENT, LuaHighlightingData.LUADOC);
    }

    @NotNull
    public Lexer getHighlightingLexer() {
        return new LuaLexer();
    }

    @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return pack(colors.get(tokenType));
  }




}
