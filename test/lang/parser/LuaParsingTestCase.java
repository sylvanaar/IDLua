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
package com.sylvanaar.idea.Lua.lang.parser;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sylvanaar.idea.Lua.util.TestUtils;

import java.util.List;

/**
 * @author peter
 */
public abstract class LuaParsingTestCase extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getBasePath() {
        return TestUtils.getTestDataPath() + "parsing/lua/";
    }

    public void doTest() {
        doTest(getTestName(true).replace('$', '/') + ".lua.test");
    }

    protected void doTest(String fileName) {
        final List<String> list = TestUtils.readInput(getBasePath() + "/" + fileName);

        final String input = list.get(0);
        final String output = list.get(1);
        checkParsing(input, output);
    }

    protected void checkParsing(String input, String output) {
        final PsiFile psiFile = TestUtils.createPseudoPhysicalLuaFile(getProject(), input);
        String psiTree = DebugUtil.psiToString(psiFile, false, true);
        assertEquals(output.trim(), psiTree.trim());
    }
}
