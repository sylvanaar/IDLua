# ![Icon][2][Lua][1] language support for [JetBrains][3] IDEs

[![Build Status](http://sylvanaar-build.asuscomm.com:8084/app/rest/builds/buildType:LuaForIdea13_BuildCheck/statusIcon)]

## Features:


 * Semantic code highlighting of identifiers: global, local, parameter, upvalue
 * Highly configurable syntax highlighting
 * Syntax checking and error highlighting
 * Code completion across all files including libraries and custom API's
 * Code completions enhanced by type inference and flow analysis
 * Support for external API definitions to enhance completion including custom function signatures
 * LuaDoc auto-generation with highlighting and folding
 * Quick Documentation (ctrl-Q/cmd-f1) for Lua APIs, and custom API's
 * Code Formatter
 * Go to definition (ctrl-click/cmd-click)
 * Find Usages, Goto Symbol
 * Lua SDK REPL Console
 * Modules support for completions (**Experimental**)
 * Structure view / Code Outline
 * Refactorings
    * Safe Delete
    * Rename Identifier
    * Introduce Variable (**Experimental**)
 * Debugger (**Experimental**)
 * Code Inspections
    * Unused assignment
    * Suspicious global creation, helps catch leaked globals
    * Unbalanced assignment statements, helps catch bugs in multiple assignment statements
    * Many more...
 * Code Intentions
    * Replace explicit string library calls like `string.len("foo")` with `("foo"):len()`
    * Many more...

--

 [1]: http://lua.org/
 [2]: http://www.lua.org/images/logo.gif
 [3]: http://www.jetbrains.com/