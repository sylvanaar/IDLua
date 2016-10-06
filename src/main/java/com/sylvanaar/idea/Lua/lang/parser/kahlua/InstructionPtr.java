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

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

class InstructionPtr {


	final int[] code;
	final int idx;
	InstructionPtr(int[] code, int idx ) {
		this.code = code;
		this.idx = idx;
	}
	int get() {
        try {
		return code[idx];
        } catch (Throwable unused) {}

        return 0;
	}
	void set(int value) {
        try {
            code[idx] = value;
        } catch (Throwable unused) {}
	}

    @Override
    public String toString() {
        ReflectionToStringBuilder sb = new ReflectionToStringBuilder(this);
        return sb.toString();
    }
}