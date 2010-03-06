/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

import com.intellij.psi.tree.TokenSet;

/**
 * Utility classdef, tha contains various useful TokenSets
 *
 * @author ilyas
 */
public abstract class TokenSets implements LuaTokenTypes {

  public static TokenSet COMMENTS_TOKEN_SET = TokenSet.create(
      SHORTCOMMENT,
      LONGCOMMENT
  );

  public static TokenSet SEPARATORS = TokenSet.create(
      SEMI
  );


  public static TokenSet WHITE_SPACE_TOKEN_SET = TokenSet.create(
      WS
  );

//
//  public static TokenSet SUSPICIOUS_EXPRESSION_STATEMENT_START_TOKEN_SET = TokenSet.create(
//      mMINUS,
//      mPLUS,
//      mLBRACK,
//      mLPAREN,
//      mLCURLY
//  );

  public static final TokenSet NUMBERS = TokenSet.create( NUMBER );


  public static final TokenSet CONSTANTS = TokenSet.create(
      NUMBER,
      TRUE,
      FALSE,
      NIL,
      LONGSTRING,
      STRING     
  );

  public static final TokenSet BUILT_IN_TYPE = TokenSet.create(

  );

  public static TokenSet KEYWORD_REFERENCE_NAMES = TokenSet.orSet(TokenSet.create(
         DO, FUNCTION,
      WITH, THEN, ELSEIF, THEN, ELSE,
      WHILE, FOR, IN, RETURN, BREAK,
      CONTINUE, LOCAL,
      REPEAT, UNTIL, END
  ), BUILT_IN_TYPE);

  public static final TokenSet PROPERTY_NAMES = TokenSet.create(NAME, LONGSTRING, STRING);

//  public static TokenSet REFERENCE_NAMES = TokenSet.orSet(KEYWORD_REFERENCE_NAMES, PROPERTY_NAMES);


  public static TokenSet VISIBILITY_MODIFIERS = TokenSet.create( LOCAL );

//  public static TokenSet MODIFIERS = TokenSet.create(
//      kABSTRACT,
//      kPRIVATE,
//      kPUBLIC,
//      kPROTECTED,
//      kSTATIC,
//      kTRANSIENT,
//      kFINAL,
//      kABSTRACT,
//      kNATIVE,
//      kSYNCHRONIZED,
//      kSTRICTFP,
//      kVOLATILE,
//      kSTRICTFP
//  );

  public static TokenSet STRING_LITERALS = TokenSet.create(
      STRING, LONGSTRING
  );
}
