///*
// * Copyright 2010 Jon S Akhtar (Sylvanaar)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package com.sylvanaar.idea.Lua.parser;
//
//import com.intellij.lang.ASTNode;
//import com.intellij.lang.PsiBuilder;
//import com.intellij.lang.PsiParser;
//import com.intellij.openapi.project.Project;
//import com.intellij.psi.tree.IElementType;
//import com.intellij.psi.tree.TokenSet;
//import org.apache.log4j.Logger;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.ArrayDeque;
//import java.util.Deque;
//
//import static com.sylvanaar.idea.Lua.parser.LuaElementTypes.*;
//import static com.sylvanaar.idea.Lua.parser.LuaElementTypes.FUNCTION_DEFINITION;
//
///**
// * Created by IntelliJ IDEA.
// * User: Max
// * Date: 07.07.2009
// * Time: 17:37:49
// */
//public class LuaParser implements PsiParser {
//    static Logger log = Logger.getLogger(LuaParser.class);
//    Project project;
//
//
//    public LuaParser(Project project) {
//        this.project = project;
//
//    }
//
//    @NotNull
//    public ASTNode parse(IElementType root, PsiBuilder builder) {
//        final LuaPsiBuilder psiBuilder = new LuaPsiBuilder(builder);
//        final PsiBuilder.Marker rootMarker = psiBuilder.mark();
//
//        while (!psiBuilder.eof()) {
//            psiBuilder.debug();
//            parseOne(psiBuilder);
//        }
//        if (root != null)
//            rootMarker.done(root);
//        return builder.getTreeBuilt();
//    }
//
//    class Scope {
//        PsiBuilder.Marker mark;
//        IElementType type;
//
//        Scope(PsiBuilder.Marker mark, IElementType type) {
//            this.mark = mark;
//            this.type = type;
//        }
//
//        void done() {
//            mark.done(type);
//        }
//    }
//
//    Deque<Scope> blockStack = new ArrayDeque<Scope>();
//
//    private ASTNode parseOne(LuaPsiBuilder builder) {
//
//        int last = 0;
//        while (!builder.eof()) {
//            boolean bNeedAdvance = true;
//
//            int start = builder.getCurrentOffset();
//
//            //PsiBuilder.Marker lookahead = builder.mark();
//
//            if (builder.compare(BLOCK_BEGIN_SET)) {
//
//
//                if (builder.compare(DO) && blockStack.peekFirst() != null && blockStack.peekFirst().type != BLOCK) {
//
//                } else {
//
//                    PsiBuilder.Marker mark = builder.mark();
//                    Scope scope = new Scope(mark, parseScope(builder));
//
//
//                    blockStack.addFirst(scope);
//                }
//
//            } else if (builder.compare((BLOCK_END_SET))) {
//
//                builder.advanceLexer();
//                Scope scope = blockStack.poll();
//                if (scope != null) scope.done();
//            } else {
//                parseIdentifier(builder);
//            }
//
//            if (builder.getCurrentOffset() == start && !builder.eof())
//                builder.advanceLexer();
//        }
//
//        return null;
//    }
//
//    private IElementType parseScope(LuaPsiBuilder builder) {
//        if (builder.compare(FUNCTION))
//            return parseFunctionDef(builder);
//
//        if (builder.compare(WHILE))
//            return parseWhileDef(builder);
//
//
//        if (builder.compare(FOR))
//            return parseForDef(builder);
//
//        return BLOCK;
//    }
//
//    private IElementType parseForDef(LuaPsiBuilder builder) {
//        builder.match(FOR, "END OF THE WORLD");
//
//        PsiBuilder.Marker mark = builder.mark();
//
//        while (!builder.eof() && !builder.compare(KEYWORDS))
//            builder.advanceLexer();
//
//        boolean numeric = true;
//
//        if (builder.compare(IN)) {
//            numeric = false;
//
////            while (!builder.eof() && !builder.compare(KEYWORDS))
////                builder.advanceLexer();
//        }
//
////        while (!builder.eof() && !builder.compare(KEYWORDS))
////            builder.advanceLexer();
////
////        if (!builder.eof())
////            builder.match(DO, "do expected");
//
//        mark.rollbackTo();
//        return numeric ? NUMERIC_FOR_BLOCK : GENERIC_FOR_BLOCK;
//    }
//
//    private IElementType parseWhileDef(LuaPsiBuilder builder) {
//        builder.match(WHILE, "END OF THE WORLD");
//
//        // PsiBuilder.Marker mark = builder.mark();
//
////        while (!builder.eof() && !builder.compare(KEYWORDS))
////            builder.advanceLexer();
////
////        builder.match(DO, "do expected");
//
//        // mark.rollbackTo();
//        return WHILE_BLOCK;
//    }
//
//    private void parseIdentifier(LuaPsiBuilder builder) {
//        PsiBuilder.Marker id = builder.mark();
//        if (!builder.compareAndEat(FUNCTION_IDENTIFIER_SET)) {
//            id.drop(); return;
//        }
//
//        while (builder.compareAndEat(FUNCTION_IDENTIFIER_SET))
//            ;
//
//        id.done(IDENTIFIER_EXPR);
//    }
//
//    private IElementType parseFunctionDef(LuaPsiBuilder builder) {
//        PsiBuilder.Marker funcStmt = builder.mark();
//        builder.compareAndEat(FUNCTION);
//
//
//        PsiBuilder.Marker funcName = builder.mark();
//
//        int pos = builder.getCurrentOffset();
//
//        while (builder.compareAndEat(FUNCTION_IDENTIFIER_SET))
//            ;
//
//        boolean anon = false;
//        if (builder.compare(LPAREN)) {
//            anon = builder.getCurrentOffset() <= pos;
//
//            if (anon) {
//                funcName.drop();
//            } else {
//                funcName.done(FUNCTION_IDENTIFIER);
//            }
//        }
//
//        builder.match(LPAREN, "expected (");
//
//        pos = builder.getCurrentOffset();
//        PsiBuilder.Marker mark = builder.mark();
//
//        parseParameterList(builder);
//
//        if (builder.getCurrentOffset() > pos)
//            mark.done(LuaElementTypes.PARAMETER_LIST);
//        else
//            mark.drop();
//
//        builder.match(RPAREN, "expected )");
//
//        funcStmt.done(anon ? ANON_FUNCTION_DEFINITION : FUNCTION_DEFINITION);
//        return anon ? ANON_FUNCTION_BLOCK : FUNCTION_BLOCK;
//    }
//
//
//    void parseParameterList(LuaPsiBuilder builder) {
//        PsiBuilder.Marker parm = builder.mark();
//        while (builder.compare(TokenSet.create(NAME, COMMA, ELLIPSIS))) {
//            if (builder.compare(NAME) || builder.compare(ELLIPSIS)) {
//                if (parm == null) {
//                    builder.error("Expected ,");
//                    builder.advanceLexer();
//                } else {
//                    builder.advanceLexer();
//                    parm.done(PARAMETER);
//                    parm = null;
//                }
//            } else if (builder.compare(COMMA)) {
//                if (parm != null)
//                    parm.error("Expected identifier or ...");
//
//                builder.advanceLexer();
//                parm = builder.mark();
//            }
//
//        }
//        ;
//        if (parm != null)
//            parm.drop();
//    }
//
//}
//
//
