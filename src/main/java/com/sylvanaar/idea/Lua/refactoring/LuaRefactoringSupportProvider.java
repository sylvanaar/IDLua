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

package com.sylvanaar.idea.Lua.refactoring;

// Does not work with version 9 and 10 at the same time


import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringActionHandler;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.refactoring.introduce.LuaIntroduceVariableHandler;

/**
* Created by IntelliJ IDEA.
* User: Jon S Akhtar
* Date: Jun 12, 2010
* Time: 4:38:09 AM
*/
public class LuaRefactoringSupportProvider extends RefactoringSupportProvider {
    @Override
    public boolean isSafeDeleteAvailable(PsiElement element) {
        return element instanceof LuaNamedElement;
    }

    @Override
    public RefactoringActionHandler getIntroduceVariableHandler() {
        return new LuaIntroduceVariableHandler();
    }
}
