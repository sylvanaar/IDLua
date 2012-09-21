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

package com.sylvanaar.idea.Lua.lang.parser;

import com.intellij.lang.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Apr 3, 2010
 * Time: 3:34:06 AM
 */
public class LuaPsiBuilder {
private PsiBuilder psiBuilder;
    private boolean isError = false;

    static Logger log = Logger.getInstance("Lua.parser.LuaPsiBuilder");

	public LuaPsiBuilder(@NotNull PsiBuilder builder) {
		psiBuilder = builder;

       // psiBuilder.setDebugMode(true);
	}

	public boolean compare(final IElementType type) {
		return getTokenType() == type;
	}

	public boolean compare(final TokenSet types) {
		return types.contains(getTokenType());
	}

    public void debug() { psiBuilder.setDebugMode(true); }
    
	public boolean compareAndEat(final IElementType type) {
		boolean found = compare(type);
		if (found) {
			advanceLexer();
		}
		return found;
	}

    public CharSequence getOriginalText() {
        return psiBuilder.getOriginalText();
    }

    public boolean compareAndEat(final TokenSet types) {
		boolean found = compare(types);
		if (found) {
			advanceLexer();
		}
		return found;
	}

//	public void match(final IElementType token) {
//		match(token, LuaParserErrors.expected(token));
//	}

    public PsiFile getFile() {
        return psiBuilder.getUserDataUnprotected(FileContextUtil.CONTAINING_FILE_KEY);
    }

	public void match(final IElementType token, final String errorMessage) {
		if (!compareAndEat(token)) {
			error(errorMessage);
		}
	}

//	public void match(final TokenSet tokens) {
//		match(tokens, LuaParserErrors.expected(tokens));
//	}

	public void match(final TokenSet tokens, final String errorMessage) {
		if (!compareAndEat(tokens)) {
			error(errorMessage);
		}
	}


	// CORE PsiBuilder FEATURES
	public void advanceLexer() {
		psiBuilder.advanceLexer();
//        log.info("advance lexer <" + psiBuilder.getTokenType() +">");
	}

	public PsiBuilder.Marker mark() {
//        log.info("mark");
		return psiBuilder.mark();
	}

	public void error(String errorMessage) {
//        setError(true);
		psiBuilder.error(errorMessage);
	}

	public IElementType getTokenType() {
//        log.info("token type <" + psiBuilder.getTokenType() +"> text <" + psiBuilder.getTokenText() + ">");
		return psiBuilder.getTokenType();
	}

    public String text() {
        return psiBuilder.getTokenText();
    }

	public boolean eof() {
		return psiBuilder.eof();
	}

	public ASTNode getTreeBuilt() {
		return psiBuilder.getTreeBuilt();
	}

	public int getCurrentOffset() {
		return psiBuilder.getCurrentOffset();
	}

//    public boolean isError() {
//        return isError;
//    }

//    public void setError(boolean error) {
//        isError = error;
//    }

    public IElementType rawLookup(int offset) {
        return psiBuilder.rawLookup(offset);
    }

    public void setWhitespaceSkippedCallback(WhitespaceSkippedCallback whitespaceSkippedCallback) {
        psiBuilder.setWhitespaceSkippedCallback(whitespaceSkippedCallback);
    }

    public int rawTokenTypeStart(int steps) {
        return psiBuilder.rawTokenTypeStart(steps);
    }
}
