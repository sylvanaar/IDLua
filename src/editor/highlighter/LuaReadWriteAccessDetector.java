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

package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/17/11
 * Time: 1:37 AM
 */
public class LuaReadWriteAccessDetector extends ReadWriteAccessDetector {
    @Override
    public boolean isReadWriteAccessible(PsiElement element) {
        return element instanceof LuaSymbol;
    }

    @Override
    public boolean isDeclarationWriteAccess(PsiElement element) {
        return element instanceof LuaDeclarationExpression;
    }

  public Access getReferenceAccess(final PsiElement referencedElement, final PsiReference reference) {
    if (reference.getElement().getParent().getParent() instanceof LuaIdentifierList)
        return Access.Write;

    return Access.Read;
  }

  public Access getExpressionAccess(final PsiElement expression) {
    if (expression.getParent().getParent() instanceof LuaIdentifierList)
        return Access.Write;

    return Access.Read;
  }
}
