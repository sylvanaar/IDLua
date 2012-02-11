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

package com.sylvanaar.idea.Lua.lang.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.LuaDocElementTypes;


/**
 * Interface that contains all tokens returned by LuaLexer
 *
 * @author sylvanaar
 */
public interface LuaTokenTypes extends LuaDocElementTypes {
    //IFileElementType FILE = new IFileElementType(Language.findInstance(LuaLanguage.class));
    /**
     * Wrong token. Use for debugger needs
     */
    IElementType WRONG = TokenType.BAD_CHARACTER;


    /* **************************************************************************************************
   *  Whitespaces & NewLines
   * ****************************************************************************************************/

    IElementType NL_BEFORE_LONGSTRING = new LuaElementType("newline after longstring stert bracket");
    IElementType WS = TokenType.WHITE_SPACE;
    IElementType NEWLINE = new LuaElementType("new line");

    TokenSet WHITE_SPACES_SET = TokenSet.create(WS, NEWLINE, TokenType.WHITE_SPACE, LDOC_WHITESPACE, NL_BEFORE_LONGSTRING);

    /* **************************************************************************************************
   *  Comments
   * ****************************************************************************************************/

    IElementType SHEBANG = new LuaElementType("shebang - should ignore");

    IElementType LONGCOMMENT = new LuaElementType("long comment");
    IElementType SHORTCOMMENT = new LuaElementType("short comment");

    IElementType LONGCOMMENT_BEGIN = new LuaElementType("long comment start bracket");
    IElementType LONGCOMMENT_END = new LuaElementType("long comment end bracket");

    TokenSet COMMENT_SET = TokenSet.create(SHORTCOMMENT, LONGCOMMENT,  SHEBANG, LUADOC_COMMENT, LONGCOMMENT_BEGIN,
            LONGCOMMENT_END);
   
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

    IElementType LONGSTRING_BEGIN = new LuaElementType("long string start bracket");
    IElementType LONGSTRING_END = new LuaElementType("long string end bracket");



    TokenSet STRING_LITERAL_SET = TokenSet.create(STRING, LONGSTRING, LONGSTRING_BEGIN, LONGSTRING_END);


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
    IElementType MOD = new LuaElementType("%");

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

    /*
    IElementType MODULE = new LuaElementType("module");
    IElementType REQUIRE = new LuaElementType("require");
    */



    TokenSet KEYWORDS = TokenSet.create(DO, FUNCTION, NOT, AND, OR,
            WITH, IF, THEN, ELSEIF, THEN, ELSE,
            WHILE, FOR, IN, RETURN, BREAK,
            CONTINUE, LOCAL,
            REPEAT, UNTIL, END/*, MODULE, REQUIRE */);

    TokenSet BRACES = TokenSet.create(LCURLY, RCURLY);
    TokenSet PARENS = TokenSet.create(LPAREN, RPAREN);
    TokenSet BRACKS = TokenSet.create(LBRACK, RBRACK);

    TokenSet BAD_INPUT = TokenSet.create(WRONG, UNTERMINATED_STRING);
    
    TokenSet DEFINED_CONSTANTS = TokenSet.create(NIL, TRUE, FALSE);

    TokenSet UNARY_OP_SET = TokenSet.create(MINUS, GETN);

    TokenSet BINARY_OP_SET = TokenSet.create(
            MINUS, PLUS, DIV, MULT, EXP, MOD,
            CONCAT);

    TokenSet COMPARE_OPS = TokenSet.create(EQ, GE, GT, LT, LE, NE);
    TokenSet LOGICAL_OPS = TokenSet.create(AND, OR, NOT);
    TokenSet ARITHMETIC_OPS = TokenSet.create(MINUS, PLUS, DIV, EXP, MOD);

    TokenSet TABLE_ACCESS = TokenSet.create(DOT, COLON, LBRACK);

      TokenSet LITERALS_SET = TokenSet.create(NUMBER, NIL, TRUE, FALSE, STRING, LONGSTRING, LONGSTRING_BEGIN, LONGSTRING_END);

    TokenSet IDENTIFIERS_SET = TokenSet.create(NAME);

    TokenSet WHITE_SPACES_OR_COMMENTS = TokenSet.orSet(WHITE_SPACES_SET, COMMENT_SET);

    TokenSet OPERATORS_SET = TokenSet.orSet(BINARY_OP_SET, UNARY_OP_SET, COMPARE_OPS, TokenSet.create(ASSIGN));
}
