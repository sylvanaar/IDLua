Lua language integration for IntelliJ IDEA
==========================================

Features:

  1. Basic Completions (Experimental)
  2. Multiple documentation providers (Experimental)
  3. Resolving Globals (Compound vars are buggy) (Experimental)
  4. Custom API Support (Experimental)
  5. Function Information via Quickhelp (Experimental)
  6. Hilighting of Upvalues and Fields (Experimental)
  7. Goto Symbol
  8. Safe Delete (Experimental)
  9. Rename Identifier (Experimental)
  10. Quick Documentation
  11. JavaHelp For Lua 5.1
  12. Execution in the Kahlua interpreter
  13. Identifier Highlighter
  14. Go to definition
  15. find usages
  16. Code formatting
  17. Keyword completion
  18. 5 code intentions(s)
  19. 6 code inspection(s)
  20. Highlighting global vs local variables
  21. Script execution and run configurations
  22. Kahlua interpreter window for interactive script execution (repl)
  23. Comes with an embedded Lua compiler written in Java (Kahlua)
  24. Structure view
  25. Syntax checking
  26. Syntax highlighting - including proper handling of extended syntax comments and quotes
  27. Customizable highlighting colors
  28. Code folding for code blocks and comments
  29. Brace Matching for do blocks, long strings and comments, and (, { , [
  30. Minor feature: comment in/out.z


Change Log
==========

   0.9.50-Beta2 Major new features, and rework of some fundamental implementations, e.g  reference resoltion, if your build is marked as beta and you find bugs, you can revert to a previous version. 
   0.9.21-23 Fix lexer errors when indexing, smart de-dent on enter 
   0.9.20 Formatter tweaks 
   0.9.19 Fix field hilights 
   0.9.18 Fix TODO handling 
   0.9.17 Disable folding for single line table constructors 
   0.9.16 fixed lexing of hexadecimal numbers 
   0.9.14 Performance enhancements 
   0.9.13 safe delete (experimental) 
   0.9.12 vesioning for IntelliJ 10 
   0.9.11 adds the rename identifier refactoring (experimental) 
   0.9.10 Fixes an edge case where too many upvalues would cause an exception 
   0.9.8-9 Fixes for multiple inspection annotations 
   0.9.1-7 Additional reference issues fixed, improved performance of editor when using the indentifier highlighter 
   0.9 Most reference issues are fixed. QuickDocs 
   0.8.33 New inspection and some bugfixes. 
   0.8.32 fix eof parsing bug 
   0.8.31 stop bug report submissions from generating another bug due to logging 
   0.8.30 enable 3 new inspections. fix a major bug in the parser that was causing exceptions saying there were unparsed tokens 
   0.8.25-9 bugfixes 
   0.8.24 Disable auto brace closing and remove the name validator     
   0.8.23 more identifier fixes                 
   0.8.22 fix resolution of identifiers used in function declarations like foo:bar(), where foo would not resolve               
   0.8.21 Adds code to try an rescue an incomplete parse   
   0.8.20 Enable identifier highlighting by default 
   0.8.19 Fixed many name resolution issues, improved brace handling             
   0.8.17 Long string literal fix 
   0.8.16 Long string literal fix   
   0.8.15 Much improved code formatting, and editor auto-indention   
   0.8.14 fixes for intentions, tail call marker, improved parser error recovery   
   0.8.13 automated error submissions   
   0.8.10-12 Many improvements to the code formatter (still considered experimental)                  
   0.8.9 Identifier highlighter, javahelp topic for lua 5.1, Kahlua execution 
   0.8.1-0.8.4 Identifier resolution fixes 
   0.8 Experimental reference resolution for go to declaration and find usages. Does not work for alot of cases. Kahlua SDK also not working 
   0.7.13-16 Fix the unbalanced assignment inspection for the single function call case, automatically select the interpreter from the SDK, fix structure view 
   0.7.12 Support for Lua SDK specification 
   0.7.11 Support for Lua Module Projects 
   0.7.10 Fixed tail call highlighting, minor bugfixes 
   0.7.9 Restore operation in Rubymine (tested) and other jetbrains products 
   0.7.6-0.7.8 Bug fixes 
   0.7.5 First inspection, global self usage 
   0.7-0.7.4 Using internal parser, instead of external annotator. 
   0.6.5 display key commands for Kahlua shell 
   0.6.3 - 0.6.4 Improve annotation when using Kahlua - now it annotates semi-instantly 
   0.6.2 Fix unresponsive annotator for syntax errors 
   0.6.1 Bug fixes, and disabling of some unimplemented features 
   0.6 Now using Kahlua as the built in interpreter tool, and the compiler     
   0.5.1 Improved the script execution interface, coded mostly from the bash plugin's implementation             
   0.5.0 Very basic script execution support. The lua interpreter needs to be on your path for this to work. Borrowed lots of code from the bash plugin             
   0.4.3 Removed some java specific code 
   0.4.2 Enabled some more experimental features, run configs, and formatting. Neither really work well at the moment 
   0.4.1 Added application level options for syntax check type 
   0.4.0 Using luaj to syntax check the current file. Optional luac in next release 
   0.3.0 Using luac to syntax check the current file when luac is on the path. 
   0.2.1 Improved parser offering better code folding for functions 
   0.2.0 Simple block parser based code folding 
   0.1.7 Fix to make eg. ----[[ a single long comment instead of a short comment followed by a long comment 
   0.1.6 Fix to ignore newline after extended string start token 
   0.1.5 Customizable coloring 