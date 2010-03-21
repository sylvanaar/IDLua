package com.sylvanaar.idea.Lua.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;

%%

//--- file: lua.l ---
/*
* lua.l - flex lexer for Lua 5.1
* Copyright: Same as Lua
*/

%class _LuaLexer
%implements FlexLexer, LuaTokenTypes

%unicode
%debug
%char
%line
%column

%function lex
%type IElementType

%eof{ return;
%eof}

%{
    int yyline, yychar, yycolumn;
    
    private StringBuffer morePrefix = new StringBuffer();
    private boolean clearMorePrefix;

    // same functionality as Flex's yymore()
    public void cleanMore() {
        this.clearMorePrefix = true;
    }
    public void more() {
        this.morePrefix.append(this.yytext());
        this.clearMorePrefix = false;
    }

    // wrapper around yytext() allowing the usage of more()
    public final String text() {
        return (this.morePrefix.toString() + this.yytext());
    }

    // wrapper around yylength() allowing the usage of more()
    public final int length() {
        return this.morePrefix.length() + this.yylength();
    }

    // wrapper around yycharat() allowing the usage of more()
    public final char charat(int pos) {
        if (pos < this.morePrefix.length()) {
            return this.morePrefix.charAt(pos);
        } else {
            return this.yycharat(pos - this.morePrefix.length());
        }
    }

    // wrapper around yylex() deleting the morePrefix
    public IElementType advance() throws java.io.IOException {
        IElementType ret = lex();
        this.morePrefix.setLength(0);
        this.clearMorePrefix = true;
        return ret;
    }


%}
%init{

   
    this.morePrefix = new StringBuffer();
    this.clearMorePrefix = true;

%init}
w           =   [ \t\v\a]+
o           =   [ \t\v\a]*
nl          =   \r|\n|\r\n
name        =   [_a-zA-Z][_a-zA-Z0-9]*
n           =   [0-9]+
exp         =   [Ee][+-]?{n}
number      =   ({n}|{n}[.]{n}){exp}?
sep         =   =*



%x XLONGSTRING
%x XSHORTCOMMENT
%x XLONGCOMMENT
%x XSTRINGQ
%x XSTRINGA

%%

/* Keywords */

"and"          { return AND; }
"break"        { return BREAK; }
"do"           { return DO; }
"else"         { return ELSE; }
"elseif"       { return ELSEIF; }
"end"          { return END; }
"false"        { return FALSE; }
"for"          { return FOR; }
"function"     { return FUNCTION; }
"if"           { return IF; }
"in"           { return IN; }
"local"        { return LOCAL; }
"nil"          { return NIL; }
"not"          { return NOT; }
"or"           { return OR; }
"repeat"       { return REPEAT; }
"return"       { return RETURN; }
"then"         { return THEN; }
"true"         { return TRUE; }
"until"        { return UNTIL; }
"while"        { return WHILE; }

{number}     { return NUMBER; }


"--[["       { more(); yybegin( XLONGCOMMENT ); }
"--"         { more(); yybegin( XSHORTCOMMENT ); }

"[["({o}\n)? { more(); yybegin( XLONGSTRING ); }

\"           { more(); yybegin(XSTRINGQ);}
'            { more(); yybegin(XSTRINGA);}

{w}          { return WS; }
"..."        { return ELLIPSIS; }
".."         { return CONCAT; }
"=="         { return EQ; }
">="         { return GE; }
"<="         { return LE; }
"~="         { return NE; }
"-"          { return MINUS; }
"+"          { return PLUS;}
"*"          { return MULT;}
"/"          { return DIV; }
"="          { return ASSIGN;}
">"          { return GT;}
"<"          { return LT;}
"("          { return LPAREN;}
")"          { return RPAREN;}
"["          { return LBRACK;}
"]"          { return RBRACK;}
"{"          { return LCURLY;}
"}"          { return RCURLY;}
"#"          { return GETN;}
","          { return COMMA; }
";"          { return SEMI; }
":"          { return COLON; }
"."          { return DOT;}
"."          { return EXP;}
{nl}         { return NEWLINE; }
\r           { return WS; }



<XSTRINGQ>
{
  \"\"       {more();}
  \"         { yybegin(YYINITIAL); return STRING; }
  \\[abfnrtv] {more();}
  \\\n       {more();}
  \\\"       {more();}
  \\'        {more();}
  \\"["      {more();}
  \\"]"      {more();}
  [\n|\r]    {
                     yybegin(YYINITIAL);

                    return WRONG;
                 }
  .          {more();}
}

<XSTRINGA>
{
  ''          {more();}
  '           { yybegin(YYINITIAL); text(); return STRING; }
  \\[abfnrtv] {more();}
  \\\n        {more();}
  \\\"        {more();}
  \\'         {more();}
  \\"["       {more();}
  \\"]"       {more();}
  [\n|\r]     {
                      yybegin(YYINITIAL);
                      return WRONG;
                  }
  .          { more();}
}

<XLONGSTRING>
{
  "]]"       { yybegin(YYINITIAL); text(); return LONGSTRING; }
  \n         {more();}
  \r         {more();}
  .          {more();}
}

<XSHORTCOMMENT>
{
  \n         {yybegin(YYINITIAL); text(); return SHORTCOMMENT; }
  \r         {yybegin(YYINITIAL); text(); return SHORTCOMMENT; }
  .          {more();}
}

<XLONGCOMMENT>
{
  "]]"     { yybegin(YYINITIAL); text(); return LONGCOMMENT; }
  \n         {more();}
  \r         {more();}
  .          {more();}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////      identifiers      ////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

{name}       { return NAME; }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////// Other ////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
.            { return WRONG; }