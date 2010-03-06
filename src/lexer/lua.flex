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


%function advance
%type IElementType

%eof{ return;
%eof}

%{

  private Stack <IElementType> gStringStack = new Stack<IElementType>();
  private Stack <IElementType> blockStack = new Stack<IElementType>();

  private int afterComment = YYINITIAL;
  private int afterNls = YYINITIAL;
  private int afterBrace = YYINITIAL;

  private void clearStacks(){
    gStringStack.clear();
    blockStack.clear();
  }

  private Stack<IElementType> braceCount = new Stack <IElementType>();

%}

w           =   [ \t\v\a]+
o           =   [ \t\v\a]*
name        =   [_a-zA-Z][_a-zA-Z0-9]*
n           =   [0-9]+
exp         =   [Ee][+-]?{n}
number      =   ({n}|{n}[.]{n}){exp}?


%x XLONGSTRING
%x XSHORTCOMMENT
%x XLONGCOMMENT
%x XSTRINGQ
%x XSTRINGA

%%

/* Keywords */
^#!.*        { return null; }
and          { return AND; }
break        { return BREAK; }
do           { return DO; }
else         { return ELSE; }
elseif       { return ELSEIF; }
end          { return END; }
false        { return FALSE; }
for          { return FOR; }
function     { return FUNCTION; }
if           { return IF; }
in           { return IN; }
local        { return LOCAL; }
nil          { return NIL; }
not          { return NOT; }
or           { return OR; }
repeat       { return REPEAT; }
return       { return RETURN; }
then         { return THEN; }
true         { return TRUE; }
until        { return UNTIL; }
while        { return WHILE; }

{number}     { return NUMBER; }


"--[["       { advance(); yybegin( XLONGCOMMENT ); }
"--"         { advance(); yybegin( XSHORTCOMMENT ); }

"[["({o}\n)? { advance(); yybegin( XLONGSTRING ); }

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
":"          { return COLON; }
"."          { return DOT;}
"."          { return EXP;}
\n           { return NEWLINE; }
\r           { return NEWLINE; }
\"           { advance(); yybegin(XSTRINGQ);}
'            { advance(); yybegin(XSTRINGA);}


<XSTRINGQ>
{
  \"\"       {advance();}
  \"         {yybegin(YYINITIAL); return STRING; }
  \\[abfnrtv] {advance();}
  \\\n       {advance();}
  \\\"       {advance();}
  \\'        {advance();}
  \\"["      {advance();}
  \\"]"      {advance();}
  [\n|\r]    {   
                     yybegin(YYINITIAL);
                     
                    return UNTERMINATED_STRING;
                 }
  .          {advance();}
}

<XSTRINGA>
{
  ''          {advance();}
  '           {yybegin(0); return STRING; }
  \\[abfnrtv] {advance();}
  \\\n        {advance();}
  \\\"        {advance();}
  \\'         {advance();}
  \\"["       {advance();}
  \\"]"       {advance();}
  [\n|\r]     {
                      yybegin(0);
                      return UNTERMINATED_STRING;
                  }
  .          { advance();}
}

<XLONGSTRING>
{
  "]]"       {yybegin(0); return LONGSTRING; }
  \n         {advance();}
  \r         {advance();}
  .          {advance();}
}

<XSHORTCOMMENT>
{
  \n         {yybegin(0); return SHORTCOMMENT; }
  \r         {yybegin(0); return SHORTCOMMENT; }
  .          {advance();}
}

<XLONGCOMMENT>
{
  "]]--"     {yybegin(0); return LONGCOMMENT; }
  \n         {advance();}
  \r         {advance();}
  .          {advance();}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////      identifiers      ////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

{name}       { return NAME; }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////// Other ////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
.            { return WRONG; }