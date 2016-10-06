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

package com.sylvanaar.idea.Lua.lang.psi.statements;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:04:59 AM
 */


/*
stat :  varlist1 '=' explist1 |
functioncall |
'do' block 'end' |
'while' exp 'do' block 'end' |
'repeat' block 'until' exp |
'if' exp 'then' block ('elseif' exp 'then' block)* ('else' block)? 'end' |
'for' NAME '=' exp ',' exp (',' exp)? 'do' block 'end' |
'for' namelist 'in' explist1 'do' block 'end' |
'function' funcname funcbody |
'local' 'function' NAME funcbody |
'local' namelist ('=' explist1)? ;
*/


public interface LuaConditionalLoop extends LuaStatementElement, LuaBlockStatement {
}
