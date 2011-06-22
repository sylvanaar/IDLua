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

import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.LightLuaTestCase;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.util.TestUtils;

import java.util.List;

/**
 * @author peter
 */
public class LuaEditingTest extends LightLuaTestCase {
    @Override
    protected String getBasePath() {
        return "editing/";
    }

    private void doTest(final char c) throws Throwable {
        final List<String> data = TestUtils.readInput(TestUtils.getTestDataPath() + getBasePath() + getTestName(true) + ".lua");

        PsiFile file = myFixture.configureByText(LuaFileType.LUA_FILE_TYPE, data.get(0));
        myFixture.type(c);

        final List<String> expectedData = TestUtils.readInput(TestUtils.getTestDataPath() + getBasePath() + getTestName(true) + "_after.lua");
        myFixture.checkResult(expectedData.get(0));
    }

    public void testLeftParenInFunctionDefinition() throws Throwable { doTest('('); }

    public void testFunctionArgInFunctionCall() throws Throwable { doTest('('); }
}