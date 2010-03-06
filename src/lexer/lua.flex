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

{number}     { echo(); return symbol(NUMBER); }
{name}       { echo(); return symbol(NAME); }

"--[["       { yymore(); yybegin( XLONGCOMMENT ); }
"--"         { yymore(); yybegin( XSHORTCOMMENT ); }

"[["({o}\n)? { yymore(); yybegin( XLONGSTRING ); }

{w}          { echo(); return symbol(WHITESPACE); }
"..."        { echo(); return symbol(ELLIPSIS); }
".."         { echo(); return symbol(CONCAT); }
"=="         { echo(); return symbol(EQ); }
">="         { echo(); return symbol(GE); }
"<="         { echo(); return symbol(LE); }
"~="         { echo(); return symbol(NE); }
"-"          {return yytext();}
"+"          {return yytext();}
"*"          {return yytext();}
"/"          {return yytext();}
"="          {return yytext();}
">"          {return yytext();}
"<"          {return yytext();}
"("          {return yytext();}
")"          {return yytext();}
"["          {return yytext();}
"]"          {return yytext();}
"{"          {return yytext();}
"}"          {return yytext();}
\n           { echo(); return symbol(NEWLINE); }
\r           { echo(); return symbol(NEWLINE); }
\"           {yymore(); yybegin(XSTRINGQ);}
'            {yymore(); yybegin(XSTRINGA);}
.            {return yytext();}

<XSTRINGQ>
{
  \"\"       {yymore();}
  \"         {yybegin(YYINITIAL); echo(); return symbol(STRING); }
  \\[abfnrtv] {yymore();}
  \\\n       {yymore();}
  \\\"       {yymore();}
  \\'        {yymore();}
  \\"["      {yymore();}
  \\"]"      {yymore();}
  [\n|\r]    {    error("unterminated string.\n");
                     yybegin(YYINITIAL);
                     echo();
                    return symbol(STRING);
                 }
  .          {yymore();}
}

<XSTRINGA>
{
  ''          {yymore();}
  '           {BEGIN(0); echo(); return symbol(STRING); }
  \\[abfnrtv] {yymore();}
  \\\n        {yymore();}
  \\\"        {yymore();}
  \\'         {yymore();}
  \\"["       {yymore();}
  \\"]"       {yymore();}
  [\n|\r]     {    error("unterminated string.\n");
                      BEGIN(0);
                      echo(); return symbol(STRING); 
                  }
  .          { yymore();}
}

<XLONGSTRING>
{
  "]]"       {BEGIN(0); echo(); return symbol(LONGSTRING); }
  \n         {yymore();}
  \r         {yymore();}
  .          {yymore();}
}

<XSHORTCOMMENT>
{
  \n         {BEGIN(0); echo(); return symbol(SHORTCOMMENT); }
  \r         {BEGIN(0); echo(); return symbol(SHORTCOMMENT); }
  .          {yymore();}
}

<XLONGCOMMENT>
{
  "]]--"     {BEGIN(0); echo(); return symbol(LONGCOMMENT); }
  \n         {yymore();}
  \r         {yymore();}
  .          {yymore();}
}

