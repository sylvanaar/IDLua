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
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 07.07.2009
 * Time: 17:37:49
 */
public class LuaParser implements PsiParser {

    Project project;

    public LuaParser(Project project) {
        this.project = project;

    }

    @NotNull
    public ASTNode parse(IElementType root, PsiBuilder builder) {
        builder.setDebugMode(true);
        final PsiBuilder.Marker rootMarker = builder.mark();
        parseFile(builder);
        rootMarker.done(root);
        return builder.getTreeBuilt();

    }

    private void parseFile(PsiBuilder builder) {

        while (!builder.eof()) {

            IElementType token = builder.getTokenType();

             builder.advanceLexer();
//            if (token == LuaElementTypes.CONTEXT_NAME || token == LuaElementTypes.DIRECTIVE_NAME) {
//                parseDirective(builder);
//            } else if (token == LuaElementTypes.CLOSING_BRACE) {
//                builder.advanceLexer();
//                builder.error(LuaBundle.message("parser.unexpected", "}"));
//            } else if (token == LuaElementTypes.OPENING_BRACE) {
//                builder.advanceLexer();
//                builder.error(LuaBundle.message("parser.unexpected", "{"));
//            } else if (token == LuaElementTypes.SEMICOLON) {
//                builder.advanceLexer();
//                builder.error(LuaBundle.message("parser.unexpected", ";"));
//            } else {
//                builder.advanceLexer();
//            }
//
        }

    }

    private void parseDirective(PsiBuilder builder) {

//        IElementType directiveNameType = builder.getTokenType(); //CONTEXT_NAME or DIRECTIVE_NAME
//        PsiBuilder.Marker directiveNameMark = builder.mark();
//        PsiBuilder.Marker directiveMark = directiveNameMark.precede();
//
//        builder.advanceLexer();
//        directiveNameMark.done(directiveNameType);
//
//        //return on eof
//        if (parseDirectiveValues(builder)) {
//            directiveMark.done(LuaElementTypes.DIRECTIVE);
//            builder.error(LuaBundle.message("parser.eof"));
//            return;
//        }
//
//        if (builder.getTokenType() == LuaElementTypes.SEMICOLON) {
//
//            builder.advanceLexer();
//            directiveMark.done(LuaElementTypes.DIRECTIVE);
//
//        } else if (builder.getTokenType() == LuaElementTypes.OPENING_BRACE) {
//
//            parseContext(builder, directiveMark);
//
//        } else {
//
//            builder.error(LuaBundle.message("parser.orexpected", ';', '{'));
//            directiveMark.done(LuaElementTypes.DIRECTIVE);
//
//        }

    }

    private boolean parseDirectiveValues(PsiBuilder builder) {

//        while (builder.getTokenType() == LuaElementTypes.DIRECTIVE_VALUE ||
//                builder.getTokenType() == LuaElementTypes.DIRECTIVE_STRING_VALUE ||
//                builder.getTokenType() == LuaElementTypes.INNER_VARIABLE ||
//                builder.getTokenType() == LuaElementTypes.VALUE_WHITE_SPACE) {
//            if (builder.eof()) {
//                return true;
//            }
//            if (builder.getTokenType() == LuaElementTypes.DIRECTIVE_VALUE || builder.getTokenType() == LuaElementTypes.INNER_VARIABLE) {
//                PsiBuilder.Marker valueMarker = builder.mark();
//                //returns true on eof
//                if (parseDirectiveValue(builder)) {
//                    valueMarker.error("valueMarker error");
//                    return true;
//                } else {
//                    valueMarker.done(LuaElementTypes.COMPLEX_VALUE);
//                }
//            } else if (builder.getTokenType() == LuaElementTypes.VALUE_WHITE_SPACE) {
//                builder.advanceLexer();
//            } else if (builder.getTokenType() == LuaElementTypes.DIRECTIVE_STRING_VALUE) {
//                PsiBuilder.Marker complexValueMark = builder.mark();
//                PsiBuilder.Marker mark = builder.mark();
//                builder.advanceLexer();
//                mark.done(LuaElementTypes.DIRECTIVE_STRING_VALUE);
//                complexValueMark.done(LuaElementTypes.COMPLEX_VALUE);
//            }
//        }
        return false;
    }

    private boolean parseDirectiveValue(PsiBuilder builder) {
//        while (builder.getTokenType() == LuaElementTypes.DIRECTIVE_VALUE || builder.getTokenType() == LuaElementTypes.INNER_VARIABLE) {
//            if (builder.eof()) {
//                return true;
//            }
//            PsiBuilder.Marker marker = builder.mark();
//            IElementType tokenType = builder.getTokenType();
//            builder.advanceLexer();
//            marker.done(tokenType);
//        }
        return false;
    }

    private void parseContext(PsiBuilder builder, PsiBuilder.Marker directiveMark) {

//        PsiBuilder.Marker contextMarker = builder.mark();
//
//        builder.advanceLexer(); // leaving { behind
//
//        while (builder.getTokenType() != LuaElementTypes.CLOSING_BRACE) {
//
//            if (builder.eof()) {
//                builder.error(LuaBundle.message("parser.expected", '}'));
//                contextMarker.done(LuaElementTypes.CONTEXT);
//                directiveMark.done(LuaElementTypes.DIRECTIVE);
//                return;
//            }
//
//            IElementType token = builder.getTokenType();
//            if (token == LuaElementTypes.CONTEXT_NAME || token == LuaElementTypes.DIRECTIVE_NAME) {
//
//                parseDirective(builder);
//
//            } else if (token == LuaElementTypes.CLOSING_BRACE) {
//
//                contextMarker.done(LuaElementTypes.CONTEXT);
//                directiveMark.done(LuaElementTypes.DIRECTIVE);
//                builder.advanceLexer();
//                return;
//
//            } else if (token == LuaElementTypes.OPENING_BRACE) {
//
//                builder.advanceLexer();
//                builder.error(LuaBundle.message("parser.unexpected", '{'));
//
//            } else if (token == LuaElementTypes.SEMICOLON) {
//
//                builder.advanceLexer();
//                builder.error(LuaBundle.message("parser.unexpected", ';'));
//
//            } else {
//
//                builder.advanceLexer();
//
//            }
//
//        }
//
//        //closing brace found.
//        builder.advanceLexer();
//        contextMarker.done(LuaElementTypes.CONTEXT);
//        directiveMark.done(LuaElementTypes.DIRECTIVE);

    }

}


