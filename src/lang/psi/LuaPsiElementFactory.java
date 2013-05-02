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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocParameterReference;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import org.jetbrains.annotations.Nullable;

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

    public abstract LuaPsiFile createLuaFile(String text);

    @Nullable
    public abstract LuaSymbol createReferenceNameFromText(String newElementName);

    public abstract LuaIdentifier createLocalNameIdentifier(String name);

    public abstract LuaIdentifier createGlobalNameIdentifier(String name);

    public abstract LuaIdentifier createFieldNameIdentifier(String name);

    @Nullable
    public abstract LuaExpression createExpressionFromText(String newExpression);

    public abstract LuaStatementElement createStatementFromText(String newStatement) ;

    public abstract PsiComment createCommentFromText(String s, PsiElement parent);

    public abstract PsiElement createWhiteSpaceFromText(String text);

    public abstract LuaDeclarationExpression createLocalNameIdentifierDecl(String s);

    @Nullable
    public abstract LuaDeclarationExpression createGlobalNameIdentifierDecl(String name);

    @Nullable
    public abstract LuaDeclarationExpression createParameterNameIdentifier(String name);

    public abstract LuaExpressionCodeFragment createExpressionCodeFragment(String text, LuaPsiElement context, boolean b);

    public abstract LuaDocComment createDocCommentFromText(String s);

    @Nullable
    public abstract LuaDocReferenceElement createDocFieldReferenceNameFromText(String elementName);

    public abstract LuaDocParameterReference createParameterDocMemberReferenceNameFromText(String elementName);

    public abstract LuaIdentifier createIdentifier(String name);
 }