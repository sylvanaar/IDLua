Lua language integration for IntelliJ IDEA
==========================================

Features:

  1. JavaHelp For Lua 5.1
  2. Execution in the Kahlua interpreter (**experimental**)
  3. Identifier Highlighter (**experimental**)
  4. Go to definition (**experimental**)
  5. find usages (**experimental**)
  6. Code formatting (**experimental**)
  7. Keyword completion
  8. 2 code inspection(s)
  9. Highlighting global vs local variables
  10. Script execution and run configurations
  11. Kahlua interpreter window for interactive script execution (repl)
  12. Comes with an embedded Lua compiler written in Java (Kahlua)
  13. Structure view
  14. Syntax checking
  15. Syntax highlighting - including proper handling of extended syntax comments and quotes
  16. Customizable highlighting colors
  17. Code folding for code blocks and comments
  18. Brace Matching for do blocks, long strings and comments, and (, { , [
  19. Minor feature: comment in/out.

Change Log
==========

    0.8.13 automated error submissions 
    0.8.10-12 Many improvements to the code formatter (still considered experimental)
    0.8.9 Identifier hilighter, javahelp topic for lua 5.1, Kahlua execution
    0.8.1-4 Identifier resolution fixes
    0.8 Experimental reference resolution for go to declaration and find usages. Does not work for alot of cases. Kahlua SDK also not working
    0.7.13-16 Fix the unbalanced assignment inspection for the single function call case, automatically select the interpreter from the SDK, fix structure view
    0.7.12 Support for Lua SDK specification
    0.7.11 Support for Lua Module Projects
    0.7.10 Fixed tail call highlighting, minor bugfixes
    0.7.9 Restore operation in Rubymine (tested) and other jetbrains products
    0.7.6-
    0.7.8 Bug fixes
    0.7.5 First inspection, global self usage
    0.7.0-4 Using internal parser, instead of external annotator.
    0.6.5 display key commands for Kahlua shell
    0.6.3-4 Improve annotation when using Kahlua - now it annotates semi-instantly
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