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

package com.sylvanaar.idea.Lua.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 14, 2010
 * Time: 7:12:06 PM
 */
public abstract class LuaPsiElementFactory {
    public static LuaPsiElementFactory getInstance(Project project) {
        return ServiceManager.getService(project, LuaPsiElementFactory.class);
    }
    public abstract PsiElement createReferenceNameFromText(String newElementName);

    public abstract ASTNode createNameIdentifier(String name);

    public abstract LuaExpression createExpressionFromText(String newExpression);

    public abstract LuaStatementElement createStatementFromText(String newStatement) ;

    public abstract PsiComment createCommentFromText(String s, PsiElement parent);

    public abstract PsiElement createWhiteSpaceFromText(String text);
}
