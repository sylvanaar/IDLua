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
//package com.sylvanaar.idea.Lua.findUsages;
//
//import com.intellij.lang.cacheBuilder.WordOccurrence;
//import com.intellij.lang.cacheBuilder.WordsScanner;
//import com.intellij.lexer.Lexer;
//import com.intellij.psi.tree.IElementType;
//import com.intellij.util.Processor;
//import com.sylvanaar.idea.Lua.lang.lexer.LuaLexer;
//import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
//
//
//class LuaWordsScanner implements WordsScanner {
//    private final Lexer myLexer;
//
//    public LuaWordsScanner() {
//        myLexer = new LuaLexer();
//    }
//
//    public void processWords(CharSequence fileText, Processor<WordOccurrence> processor) {
//        myLexer.start(fileText);
//        WordOccurrence occurrence = null; // shared occurrence
//
//        while (myLexer.getTokenType() != null) {
//            final IElementType type = myLexer.getTokenType();
//            if (type == LuaTokenTypes.NAME || LuaTokenTypes.KEYWORD_REFERENCE_NAMES.contains(type)) {
//                if (occurrence == null)
//                    occurrence = new WordOccurrence(fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.CODE);
//                else
//                    occurrence.init(fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.CODE);
//                if (!processor.process(occurrence)) return;
//            } else if (LuaTokenTypes.COMMENT_SET.contains(type)) {
//                if (!stripWords(processor, fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.COMMENTS, occurrence))
//                    return;
//            } else if (LuaTokenTypes.STRING_LITERAL_SET.contains(type)) {
//                if (!stripWords(processor, fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.LITERALS, occurrence))
//                    return;
//
////                if (type == LuaTokenTypes.STRING) {
////                    if (!stripWords(processor, fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.CODE, occurrence))
////                        return;
////                }
//            }
//
//            myLexer.advance();
//        }
//    }
//
//    private static boolean stripWords(final Processor<WordOccurrence> processor,
//                                      final CharSequence tokenText,
//                                      int from,
//                                      int to,
//                                      final WordOccurrence.Kind kind,
//                                      WordOccurrence occurrence) {
//        // This code seems strange but it is more effective as Character.isJavaIdentifier_xxx_ is quite costly operation due to unicode
//        int index = from;
//
//        ScanWordsLoop:
//        while (true) {
//            while (true) {
//                if (index == to) break ScanWordsLoop;
//                char c = tokenText.charAt(index);
//                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||  c == '_' ) {
//                    break;
//                }
//                index++;
//            }
//            int index1 = index;
//            while (true) {
//                index++;
//                if (index == to) break;
//                char c = tokenText.charAt(index);
//                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')
//                    continue;
//
//                break;
//            }
//
//            if (occurrence == null) occurrence = new WordOccurrence(tokenText, index1, index, kind);
//            else occurrence.init(tokenText, index1, index, kind);
//            if (!processor.process(occurrence)) return false;
//        }
//        return true;
//    }
//}