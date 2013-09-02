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

package com.sylvanaar.idea.Lua;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.sylvanaar.idea.Lua.util.TestUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public abstract class LightLuaTestCase extends LightPlatformCodeInsightFixtureTestCase {
//    public LightLuaTestCase() {
////        initPlatformPrefix("idea");
//        PlatformTestCase.initPlatformLangPrefix();
//    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(getBasePath());
    }

    @Override
    @NotNull
    protected LightProjectDescriptor getProjectDescriptor() {
        return LuaLightProjectDescriptor.INSTANCE;
    }

    /**
     * Return relative path to the test data. Path is relative to the
     * {@link com.intellij.openapi.application.PathManager#getHomePath()}
     *
     * @return relative path to the test data.
     */
    @Override
    @NonNls
    protected String getBasePath() {
        return TestUtils.getTestDataPath();
    }



}
