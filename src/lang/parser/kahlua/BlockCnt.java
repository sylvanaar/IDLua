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

package com.sylvanaar.idea.Lua.lang.parser.kahlua;

public class BlockCnt {
	BlockCnt previous;  /* chain */
	int breaklist;  /* list of jumps out of this loop */
	int nactvar;  /* # active locals outside the breakable structure */
	boolean upval;  /* true if some variable in the block is an upvalue */
	boolean isbreakable;  /* true if `block' is a loop */
}