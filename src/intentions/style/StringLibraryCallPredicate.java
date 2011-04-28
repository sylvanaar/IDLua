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

package com.sylvanaar.idea.Lua.intentions.style;

import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.intentions.base.PsiElementPredicate;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFunctionCallExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/21/11
 * Time: 2:11 PM
 */
public class StringLibraryCallPredicate implements PsiElementPredicate {
    @Override
    public boolean satisfiedBy(PsiElement element) {
        if ( ! (element instanceof LuaFunctionCallExpression ) )
            return false;

        LuaFunctionCallExpression call = (LuaFunctionCallExpression) element;

        LuaReferenceElement ref = call.getFunctionNameElement();
        String calledFunc = null;

        if (ref != null)
            calledFunc = ref.getName();

        return calledFunc != null && calledFunc.startsWith("string.");

    }
}
