Change Log
==========
    1.0a55-59 IDEA 13 Support
    1.0a42-54 Intermission, go get a popcorn.
    1.0a41 Many fixes to path handling in the run configurations
    1.0a40 Improvements to completions and type inferences
    1.0a26-39 Bug Fixes
    1.0a24-5 Introduces type inference based completions. Very experimental. Large number of pending fixes, disabled by default
    1.0a20-21 Bug Fixes
    1.0a17-19 IntelliJ 11 Support
    1.0a16 Bug fixes
    1.0a15 Major performance fixes for various global variable cases
    1.0a14 Introduce Variable Refactoring (**Experimental**)
    1.0a12-3 Bug fixes
    1.0a11 Added the "suspicious global creation" and "Parameter hides implicit self" inspections
    1.0a10 Fix the error reporter and language console up down arrow keys to cylcle through command history
    1.0a06-9 Minor fixes
    1.0a03-5 Modules support
    1.0a01-2 Debugger support
    0.9.94-5 Misc Fixes
    0.9.93 Field renaming and luadoc generation for tables
    0.9.92 Fix renaming of parameters
    0.9.91 Bug fixes. improved the unbalanced assignment handling. Doclua enhancement for Corona
    0.9.90 Fix Refactor|Rename not working for parameters
    0.9.89 Fix for PyCharm compatibility issue.
    0.9.87-8 Formatter fixes for comments
    0.9.86a Disabled the copyright optional dependency - it was not optional for everyone
    0.9.86 Extremely aggressive completions as an option "Enable Additional Completions"
    0.9.85 Autogenerate LuaDoc function comment on ---ENTER
    0.9.84 LuaDoc improvements
    0.9.83 Copied the grooovydoc implementation for use with LuaDoc. Attempt at identifier resolution in modules. Copyright support
    0.9.82 Complete revamp of the API documentation system.
    0.9.81 LuaDoc highlighting and folding. Bugfix for missing intention examples
    0.9.80 EAP 10.5 Support, Lua SDK version determined via lua.exe vs luac.exe (LuaJIT 2.0 support)
    0.9.79 Major parsing speed improvement. New intention for string.* function calls
    0.9.78 Basic language console implementation for REPL with the standard lua interpreter
    0.9.77 Memory usage tuning. Removed some caching
    0.9.75-6 New SDK implementation (You must recreate your SDK's). Added option to allow help and parameter completion for upvalued/aliased globals
    0.9.74 Pluggable quickhelp API (lua based)
    0.9.72-3 Starter set of 4 live templates fork, fori, ift, tnil (type and press tab)
    0.9.71 Point Lua documenter at the lua.org docs
    0.9.70 Handling of "self" in compeltions and reference resolution
    0.9.67-9 Smart dedent causing trouble again. So it is disabled for now
    0.9.66 Undo a change which made projects non-creatable
    0.9.65 Don't create src directory for new projects.
    0.9.64 Change the error reporter to use YouTrack while they are offering free hosting.
    0.9.63 Fix for resolve, docs, and completions when the project SDK is not set, but the module sdk is.
    0.9.62 Fix for autopopup completions outside of a statement
    0.9.61 Fix for custom SDK's with compound identifiers e.g. foo.bar
    0.9.60 Quick and dirty self: completions
    0.9.59 Quickfix for unbalanced assignment, new inspection for access of array element 0
    0.9.58 Lightning fast, stub based identifier resolving
    0.9.57 Support for 3rd party library signature files, and display of parameter infos
    0.9.53-6 Support for 3rd party libraries via the SDK classpath, Lua quickhelp (ctrl-q) from the html lua docs
    0.9.51 Compound reference support including standard Lua functions, e.g. io.write()
    0.9.50-Beta1-7 Major new features, and rework of some fundamental implementations, e.g  reference resolution, if your build is marked as beta and you find bugs, you can revert to a previous version.
    0.9.21-23 Fix lexer errors when indexing, smart de-dent on enter
    0.9.20 Formatter tweaks
    0.9.19 Fix field highlights
    0.9.18 Fix TODO handling
    0.9.17 Disable folding for single line table constructors
    0.9.16 fixed lexing of hexadecimal numbers
    0.9.14 Performance enhancements
    0.9.13 safe delete (**Experimental**)
    0.9.12 versioning for IntelliJ 10
    0.9.11 adds the rename identifier refactoring (**Experimental**)
    0.9.10 Fixes an edge case where too many upvalues would cause an exception
    0.9.8-9 Fixes for multiple inspection annotations
    0.9.1-7 Additional reference issues fixed, improved performance of editor when using the identifier highlighter
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