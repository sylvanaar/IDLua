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

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaKeyValueInitializer;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 22, 2010
 * Time: 12:10:02 AM
 */
public class LuaKeyValueInitializerImpl extends LuaExpressionImpl implements LuaKeyValueInitializer {
    public LuaKeyValueInitializerImpl(ASTNode node) {
        super(node);

//
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitKeyValueInitializer(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitKeyValueInitializer(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    protected String getExpressionLabel() {
        return "Key/Value";
    }

    @Override
    public LuaExpression getFieldKey() {
       return (LuaExpression) getChildren()[0];
    }

    @Override
    public LuaExpression getFieldValue() {
        return (LuaExpression) getChildren()[1];
    }

    @Override
    public void inferTypes() {
        if (getFieldKey() instanceof LuaFieldIdentifier) {
            LuaFieldIdentifier field = (LuaFieldIdentifier) getFieldKey();

            field.setLuaType(getFieldValue().getLuaType());
        }
    }
}
