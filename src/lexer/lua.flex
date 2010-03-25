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

%function advance
%type IElementType

%eof{ return;
%eof}

%{
    int yyline, yychar, yycolumn;

%}
%init{

   
//    morePrefix = new StringBuffer();
//    clearMorePrefix = true;

%init}
w           =   [ \t\v\f]+
o           =   [ \t\v\f]*
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


"--["{sep}"[" { yybegin( XLONGCOMMENT ); return LONGCOMMENT; }
"--"         { yybegin( XSHORTCOMMENT ); return SHORTCOMMENT; }

"["{sep}"["({o}\n)? { yybegin( XLONGSTRING ); }

"\""           { yybegin(XSTRINGQ);  return STRING; }
'            { yybegin(XSTRINGA); return STRING; }

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
"%"          { return MOD;}
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
"^"          { return EXP;}
{nl}         { return NEWLINE; }
\r           { return WS; }



<XSTRINGQ>
{
  \"\"       {return STRING;}
  \"         { yybegin(YYINITIAL); return STRING; }
  \\[abfnrtv] {return STRING;}
  \\\n       {return STRING;}
  \\\"       { yybegin(YYINITIAL); return STRING; }
  \\'        {return STRING;}
  \\"["      {return STRING;}
  \\"]"      {return STRING;}
   \\\\        { return STRING; }
  [\n\r]    { yybegin(YYINITIAL); return WRONG; }
  .          {return STRING;}
}

<XSTRINGA>
{
  ''          { return STRING; }
  '           { yybegin(YYINITIAL); return STRING; }
  \\[abfnrtv] { return STRING; }
  \\\n        { return STRING; }
  \\\'          { return STRING; }
  \\'          { yybegin(YYINITIAL); return STRING; }
  \\"["       { return STRING; }
  \\"]"       { return STRING; }
  \\\\        { return STRING; }
  [\n\r]     { yybegin(YYINITIAL);return WRONG;  }
  .          { return STRING; }
}

<XLONGSTRING>
{
  "]"{sep}"]"       { yybegin(YYINITIAL); return LONGSTRING; }
  \n         { return LONGSTRING; }
  \r         { return LONGSTRING; }
  .          { return LONGSTRING; }
}

<XSHORTCOMMENT>
{
  \n         {yybegin(YYINITIAL); return SHORTCOMMENT; }
  \r         {yybegin(YYINITIAL); return SHORTCOMMENT; }
  .          { return SHORTCOMMENT;}
}

<XLONGCOMMENT>
{
  "]"{sep}"]"     { yybegin(YYINITIAL); return LONGCOMMENT; }
  \n         { return LONGCOMMENT;}
  \r         { return LONGCOMMENT;}
  .          { return LONGCOMMENT;}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////      identifiers      ////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

{name}       { return NAME; }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////// Other ////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
.            { return WRONG; }