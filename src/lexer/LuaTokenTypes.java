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

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;


/**
 * Interface that contains all tokens returned by LuaLexer
 *
 * @author ilyas
 */
public interface LuaTokenTypes extends LuaElementTypes{

  /**
   * Wrong token. Use for debug needs
   */
  IElementType WRONG = new LuaElementType("wrong token");

  /* **************************************************************************************************
 *  Whitespaces & NewLines
 * ****************************************************************************************************/

  IElementType WS = new LuaElementType("white space");
  IElementType NEWLINE = new LuaElementType("new line");
  TokenSet WHITE_SPACES_SET=TokenSet.create(WS, NEWLINE, TokenType.WHITE_SPACE);

  /* **************************************************************************************************
 *  Comments
 * ****************************************************************************************************/

  IElementType LONGCOMMENT = new LuaElementType("long comment");
  IElementType SHORTCOMMENT = new LuaElementType("short comment");

  TokenSet COMMENT_SET = TokenSet.create(SHORTCOMMENT, LONGCOMMENT);

  /* **************************************************************************************************
 *  Identifiers
 * ****************************************************************************************************/

  IElementType NAME = new LuaElementType("identifier");

  /* **************************************************************************************************
 *  Integers & floats
 * ****************************************************************************************************/

  IElementType NUMBER = new LuaElementType("number");

  /* **************************************************************************************************
 *  Strings & regular expressions
 * ****************************************************************************************************/

  IElementType STRING = new LuaElementType("string");
  IElementType LONGSTRING = new LuaElementType("long string");
  TokenSet STRING_LITERAL_SET = TokenSet.create(STRING, LONGSTRING);


  IElementType UNTERMINATED_STRING = new LuaElementType("unterminated string");


  /* **************************************************************************************************
 *  Common tokens: operators, braces etc.
 * ****************************************************************************************************/


  IElementType DIV = new LuaElementType("/");
  IElementType MULT = new LuaElementType("*");    
  IElementType LPAREN = new LuaElementType("(");
  IElementType RPAREN = new LuaElementType(")");
  IElementType LBRACK = new LuaElementType("[");
  IElementType RBRACK = new LuaElementType("]");
  IElementType LCURLY = new LuaElementType("{");
  IElementType RCURLY = new LuaElementType("}");
  IElementType COLON = new LuaElementType(":");
  IElementType COMMA = new LuaElementType(",");
  IElementType DOT = new LuaElementType(".");
  IElementType ASSIGN = new LuaElementType("=");
  IElementType SEMI = new LuaElementType(";");
  IElementType EQ = new LuaElementType("==");
  IElementType NE = new LuaElementType("~=");
  IElementType PLUS = new LuaElementType("+");
  IElementType MINUS = new LuaElementType("-");
  IElementType GE = new LuaElementType(">=");
  IElementType GT = new LuaElementType(">");
  IElementType EXP = new LuaElementType("^");
  IElementType LE = new LuaElementType("<=");
  IElementType LT = new LuaElementType("<");
  IElementType ELLIPSIS = new LuaElementType("...");
  IElementType CONCAT = new LuaElementType("..");
  IElementType GETN = new LuaElementType("#");

  /* **************************************************************************************************
 *  Keywords
 * ****************************************************************************************************/


  IElementType IF = new LuaElementType("if");
  IElementType ELSE = new LuaElementType("else");
  IElementType ELSEIF = new LuaElementType("elseif");
  IElementType WHILE = new LuaElementType("while");
  IElementType WITH = new LuaElementType("with");

  IElementType THEN = new LuaElementType("then");
  IElementType FOR = new LuaElementType("for");
  IElementType IN = new LuaElementType("in");
  IElementType RETURN = new LuaElementType("return");
  IElementType BREAK = new LuaElementType("break");

  IElementType CONTINUE = new LuaElementType("continue");
  IElementType TRUE = new LuaElementType("true");
  IElementType FALSE = new LuaElementType("false");
  IElementType NIL = new LuaElementType("nil");
  IElementType FUNCTION = new LuaElementType("function");

  IElementType DO = new LuaElementType("do");
  IElementType NOT = new LuaElementType("not");
  IElementType AND = new LuaElementType("and");
  IElementType OR = new LuaElementType("or");
  IElementType LOCAL = new LuaElementType("local");

  IElementType REPEAT = new LuaElementType("repeat");
  IElementType UNTIL = new LuaElementType("until");
  IElementType END = new LuaElementType("end");

  TokenSet KEYWORDS = TokenSet.create( DO, FUNCTION, NOT, AND, OR,
      WITH, THEN, ELSEIF, THEN, ELSE,
      WHILE, FOR, IN, RETURN, BREAK,
      CONTINUE, TRUE, FALSE, NIL, LOCAL,
      REPEAT, UNTIL, END);

  TokenSet BRACES = TokenSet.create(LBRACK, RBRACK, LPAREN, RPAREN, LCURLY, RCURLY);

  TokenSet ASSIGN_OP_SET = TokenSet.create(ASSIGN);
      

  TokenSet UNARY_OP_SET = TokenSet.create(NOT, MINUS, GETN);

  TokenSet BINARY_OP_SET = TokenSet.create(AND, OR,
          EQ, GE, GT, LT, LE, NE,
       MINUS, PLUS, DIV, MULT, EXP);

  TokenSet DOTS = TokenSet.create( DOT );

  TokenSet WHITE_SPACES_OR_COMMENTS=TokenSet.orSet(WHITE_SPACES_SET, COMMENT_SET);
}
