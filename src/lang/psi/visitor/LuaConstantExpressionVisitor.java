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

package com.sylvanaar.idea.Lua.lang.psi.visitor;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.StringInterner;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaBinaryExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaLiteralExpression;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 10/20/11
 * Time: 6:14 PM
 */
public class LuaConstantExpressionVisitor extends LuaElementVisitor {
    private final StringInterner myInterner = new StringInterner();
    private Set<LuaSymbol> myVisitedVars;
    private Object         myResult;

    LuaConstantExpressionVisitor(Set<LuaSymbol> visitedVars) {
        myVisitedVars = visitedVars;
    }

    Object handle(PsiElement element) {
        myResult = null;
        element.accept(this);
        store(element, myResult);
        return myResult;
    }

    private static final Key<Object> VALUE = Key.create("VALUE");

    private static Object getStoredValue(PsiElement element) {
        if (element == null) {
            return null;
        }
        try {
            return element.getUserData(VALUE);
        } finally {
            element.putUserData(VALUE, null);
        }
    }

    static void store(PsiElement element, Object value) {
        element.putUserData(VALUE, value);
    }


    @Override
    public void visitLiteralExpression(LuaLiteralExpression e) {
        final Object value = e.getValue();
        myResult = value instanceof String ? myInterner.intern((String) value) : value;
    }

    @Override
    public void visitBinaryExpression(LuaBinaryExpression e) {
        super.visitBinaryExpression(e);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
