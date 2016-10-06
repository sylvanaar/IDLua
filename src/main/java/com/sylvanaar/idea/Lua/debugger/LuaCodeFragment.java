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

package com.sylvanaar.idea.Lua.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.testFramework.LightVirtualFile;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiFileImpl;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 5/15/11
 * Time: 3:04 AM
 */
public class LuaCodeFragment extends LuaPsiFileImpl {
    public LuaCodeFragment(Project project, CharSequence text) {
        super(new SingleRootFileViewProvider(PsiManager.getInstance(project),
                                             new LightVirtualFile("DebugExpression.lua", LuaFileType.LUA_FILE_TYPE,
                                                                  text), true));
        ((SingleRootFileViewProvider) getViewProvider()).forceCachedPsi(this);
    }
}
