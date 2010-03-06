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

package com.sylvanaar.idea.Lua.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 06.07.2009
 * Time: 16:40:05
 */
public class LuaSyntaxHighlighter extends SyntaxHighlighterBase {

    private Lexer lexer;

    private final TextAttributesKey[] BAD_CHARACTER_KEYS = new TextAttributesKey[]{HighlighterColors.BAD_CHARACTER};
    private final Map<IElementType, TextAttributesKey> colors = new HashMap<IElementType, TextAttributesKey>();

    public LuaSyntaxHighlighter() {

        lexer = new FlexAdapter(new _LuaLexer((java.io.Reader) null));

        colors.put(LuaElementTypes.BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);
        colors.put(LuaElementTypes.COMMENT, SyntaxHighlighterColors.JAVA_BLOCK_COMMENT);

        colors.put(LuaElementTypes.CONTEXT_NAME, SyntaxHighlighterColors.KEYWORD);
        colors.put(LuaElementTypes.DIRECTIVE_STRING_VALUE, SyntaxHighlighterColors.STRING);
        colors.put(LuaElementTypes.INNER_VARIABLE, SyntaxHighlighterColors.NUMBER);

    }

    @NotNull
    public Lexer getHighlightingLexer() {
        return lexer;
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType iElementType) {

        TextAttributesKey[] textAttributesKeys = {colors.get(iElementType)};
        if (textAttributesKeys == null) {
            textAttributesKeys = BAD_CHARACTER_KEYS;
        }
        return textAttributesKeys;

    }

}
