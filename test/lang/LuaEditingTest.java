/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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
package com.sylvanaar.idea.Lua.lang;

import com.intellij.codeInsight.CodeInsightSettings;
import com.sylvanaar.idea.Lua.LightLuaTestCase;

/**
 * @author peter
 */
public class LuaEditingTest extends LightLuaTestCase {
    private static final String TEST_DATA_FOLDER = "editing/";

    @Override
    protected String getBasePath() {
        return super.getBasePath() + TEST_DATA_FOLDER;
    }

    private void doTest(final char c) throws Throwable {
        myFixture.configureByFile(getTestName(false) + ".lua");
        myFixture.type(c);
        myFixture.checkResultByFile(getTestName(false) + "_after.lua");
    }
    private void doTest(final CharSequence cs) throws Throwable {
        myFixture.configureByFile(getTestName(false) + ".lua");
        for (int i = 0; i < cs.length(); i++) {
            myFixture.type(cs.charAt(i));
        }
        myFixture.checkResultByFile(getTestName(false) + "_after.lua");
    }

    public void testLeftParenInFunctionDefinition() throws Throwable { doTest('('); }

    public void testEndOuterReturn() throws Throwable {
        if (CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER)
            doTest('\n');
    }

    public void testEndInnerReturn() throws Throwable { doTest('\n'); }

    public void testFunctionArgInFunctionCall() throws Throwable { doTest('('); }

    public void testEndInnerReturn2() throws Throwable { doTest('\n'); }

    public void testEnterAfterRightCurly1() throws Throwable { doTest('\n'); }


}