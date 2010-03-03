import java_cup.runtime.*;
import java;
import java.io;

%%

//--- file: lua.l ---
/*
* lua.l - flex lexer for Lua 5.1
* Copyright: Same as Lua
*/

%class Lexer
%unicode
%cup
%line
%column


%{
    private void echo() { System.out.print(yytext()); }
    private void echo(String s) { System.out.print(s); }
    private void error(String s) { throw new Error(s); }

    public int position () { return yycolumn; }

    StringBuffer string = new StringBuffer();

    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
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
^#!.*        { echo("skipping: %s\n", yytext); }
and          { echo(); return symbol(AND); }
break        { echo(); return symbol(BREAK); }
do           { echo(); return symbol(DO); }
else         { echo(); return symbol(ELSE); }
elseif       { echo(); return symbol(ELSEIF); }
end          { echo(); return symbol(END); }
false        { echo(); return symbol(FALSE); }
for          { echo(); return symbol(FOR); }
function     { echo(); return symbol(FUNCTION); }
if           { echo(); return symbol(IF); }
in           { echo(); return symbol(IN); }
local        { echo(); return symbol(LOCAL); }
nil          { echo(); return symbol(NIL); }
not          { echo(); return symbol(NOT); }
or           { echo(); return symbol(OR); }
repeat       { echo(); return symbol(REPEAT); }
return       { echo(); return symbol(RETURN); }
then         { echo(); return symbol(THEN); }
true         { echo(); return symbol(TRUE); }
until        { echo(); return symbol(UNTIL); }
while        { echo(); return symbol(WHILE); }

{number}     { echo(); return symbol(NUMBER); }
{name}       { echo(); return symbol(NAME); }

"--[["       { yymore(); yybegin( XLONGCOMMENT ); }
"--"         { yymore(); yybegin( XSHORTCOMMENT ); }

"[["({o}\n)? { yymore(); yybegin( XLONGSTRING ); }

{w}          { echo(); return symbol(WHITESPACE); }
"..."        { echo(); return symbol(DOTS); }
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
  ''         {yymore();}
  '          {BEGIN(0); echo(); return symbol(STRING); }
  \\[abfnrtv]{yymore();}
  \\\n       {yymore();}
  \\\"       {yymore();}
  \\'        {yymore();}
  \\"["      {yymore();}
  \\"]"      {yymore();}
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

%%


/*
#ifdef YYMAIN

#include <stdio.h>
extern FILE*yyin,*yyout;

char* TokenName(int t)
{
  static char buffer[80];
  if( t < 0 || t == 256 ) return "<ERROR>";
  if( t == 0 ) return "EOF";
  if( t < 256 )
  {  sprintf( buffer, "CHAR %c", (unsigned char)(unsigned int)t );
     return (char*)buffer;
  }
  switch(t)
  {  case TK_AND:            return "AND";
     case TK_BREAK:          return "BREAK";
     case TK_DO:             return "DO";
     case TK_ELSE:           return "ELSE";
     case TK_ELSEIF:         return "ELSEIF";
     case TK_END:            return "END";
     case TK_FALSE:          return "FALSE";
     case TK_FOR:            return "FOR";
     case TK_FUNCTION:       return "FUNCTION";
     case TK_IF:             return "IF";
     case TK_IN:             return "IN";
     case TK_LOCAL:          return "LOCAL";
     case TK_NIL:            return "NIL";
     case TK_NOT:            return "NOT";
     case TK_OR:             return "OR";
     case TK_REPEAT:         return "REPEAT";
     case TK_RETURN:         return "RETURN";
     case TK_THEN:           return "THEN";
     case TK_TRUE:           return "TRUE";
     case TK_UNTIL:          return "UNTIL";
     case TK_WHILE:          return "WHILE";
     case TK_CONCAT:         return "CONCAT";
     case TK_DOTS:           return "DOTS";
     case TK_EQ:             return "EQ";
     case TK_GE:             return "GE";
     case TK_LE:             return "LE";
     case TK_NE:             return "NE";
     case TK_NUMBER:         return "NUMBER";
     case TK_NAME:           return "NAME";
     case TK_STRING:         return "STRING";
     case TK_LONGSTRING:     return "LONGSTRING";
     case TK_SHORTCOMMENT:   return "SHORTCOMMENT;";
     case TK_LONGCOMMENT:    return "LONGCOMMENT;";
     case TK_WHITESPACE:     return "WHITESPACE";
     case TK_NEWLINE:        return "NEWLINE";
     case TK_BADCHAR:        return "BADCHAR";
     default: break;
  }
  sprintf( buffer, "<? %d>", t );
  return buffer;
}

static class foo {
int main( int argc, char ** argv )
{
  int tok;

  yyin  = (argc>1) ? fopen(argv[1],"rt") : 0;
  yyout = (argc>2) ? fopen(argv[2],"wt") : 0;

  for( tok=yylex() ; tok ; tok=yylex() )
  {
     if( tok == TK_NEWLINE ) continue;
     if( tok == TK_WHITESPACE ) continue;
     fprintf( yyout, "%03d %-13.13s: %s\n", tok, TokenName(tok), yytext );
  }
  if(yyin!=stdin)fclose(yyin);
  if(yyout!=stdout)fclose(stdout);
  return 0;
}
}

/**/

--- file: lua.y ---
/*
* Grammar for Lua 5.1
* Dummy for now. Used to define token values.
*/
        /*
%{

%}

%token TK_EOF 0

%token TK_AND 257
%token TK_BREAK
%token TK_DO
%token TK_ELSE
%token TK_ELSEIF
%token TK_END
%token TK_FALSE
%token TK_FOR
%token TK_FUNCTION
%token TK_IF
%token TK_IN
%token TK_LOCAL
%token TK_NIL
%token TK_NOT
%token TK_OR
%token TK_REPEAT
%token TK_RETURN
%token TK_THEN
%token TK_TRUE
%token TK_UNTIL
%token TK_WHILE

%token TK_CONCAT
%token TK_DOTS
%token TK_EQ
%token TK_GE
%token TK_LE
%token TK_NE
%token TK_NUMBER
%token TK_NAME
%token TK_STRING

%token TK_LONGSTRING
%token TK_SHORTCOMMENT;
%token TK_LONGCOMMENT;
%token TK_WHITESPACE;
%token TK_NEWLINE;
%token TK_BADCHAR;

%%

start    :  Lua
        ;

Lua      :
        ;


%%

int yymain( int argc, char ** argv )
{
  return 0;
}
          */
/**/

--- file: m.cmd ---
byacc -d lua.y
rem flex -Bs8 -Cef -oylex.c lua.l
flex -Bs8 -Cem -oylex.c lua.l
cl -MD -Ox -DYYMAIN ylex.c -link /opt:ref /opt:icf /opt:nowin98

--- file: y.tab.h ---
#ifndef YYERRCODE
#define YYERRCODE 256
#endif

#define TK_EOF 0
#define TK_AND 257
#define TK_BREAK 258
#define TK_DO 259
#define TK_ELSE 260
#define TK_ELSEIF 261
#define TK_END 262
#define TK_FALSE 263
#define TK_FOR 264
#define TK_FUNCTION 265
#define TK_IF 266
#define TK_IN 267
#define TK_LOCAL 268
#define TK_NIL 269
#define TK_NOT 270
#define TK_OR 271
#define TK_REPEAT 272
#define TK_RETURN 273
#define TK_THEN 274
#define TK_TRUE 275
#define TK_UNTIL 276
#define TK_WHILE 277
#define TK_CONCAT 278
#define TK_DOTS 279
#define TK_EQ 280
#define TK_GE 281
#define TK_LE 282
#define TK_NE 283
#define TK_NUMBER 284
#define TK_NAME 285
#define TK_STRING 286
#define TK_LONGSTRING 287
#define TK_SHORTCOMMENT 288
#define TK_LONGCOMMENT 289
#define TK_WHITESPACE 290
#define TK_NEWLINE 291
#define TK_BADCHAR 292

/* eof */

*/