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


public class ExpDesc {
	int k; // expkind, from enumerated list, above

	int info, aux;
	private double _nval;
	private boolean has_nval;
	public void setNval(double r) {
		_nval = r;
		has_nval = true;
	}
	public double nval() {
		return has_nval ? _nval : info;
	}

	int t; /* patch list of `exit when true' */
	int f; /* patch list of `exit when false' */
	void init( int k, int i ) {
		this.f = KahluaParser.NO_JUMP;
		this.t = KahluaParser.NO_JUMP;
		this.k = k;
		this.info = i;
	}

	boolean hasjumps() {
		return (t != f);
	}

	boolean isnumeral() {
		return (k == KahluaParser.VKNUM && t == KahluaParser.NO_JUMP && f == KahluaParser.NO_JUMP);
	}

	public void setvalue(ExpDesc other) {
		this.k = other.k;
		this._nval = other._nval;
		this.has_nval = other.has_nval;
		this.info = other.info;
		this.aux = other.aux;
		this.t = other.t;
		this.f = other.f;
	}
}