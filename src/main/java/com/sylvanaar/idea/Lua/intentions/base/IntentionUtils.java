/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sylvanaar.idea.Lua.intentions.base;

import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElementFactory;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


/**
 * User: Dmitry.Krasilschikov
 * Date: 13.11.2007
 */
public class IntentionUtils {

    public static LuaExpression replaceExpression(@NotNull String newExpression,
                                         @NotNull LuaExpression expression) throws IncorrectOperationException {
        final LuaPsiElementFactory factory = LuaPsiElementFactory.getInstance(expression.getProject());
        final LuaExpression newCall = factory.createExpressionFromText(newExpression);
        return (LuaExpression) expression.replaceWithExpression(newCall, true);
    }

    public static LuaStatementElement replaceStatement(@NonNls @NotNull String newStatement,
                                                       @NonNls @NotNull LuaStatementElement statement) throws
            IncorrectOperationException {
        final LuaPsiElementFactory factory = LuaPsiElementFactory.getInstance(statement.getProject());
        final LuaStatementElement newCall = (LuaStatementElement) factory.createStatementFromText(newStatement);
        return statement.replaceWithStatement(newCall);
    }

//  public static void createTemplateForMethod(PsiType[] argTypes,
//                                             ChooseTypeExpression[] paramTypesExpressions,
//                                             LuaFunctionDefinitionStatement method,
//                                             GrMemberOwner owner,
//                                             TypeConstraint[] constraints, boolean isConstructor) {
//
//    Project project = owner.getProject();
//    GrTypeElement typeElement = method.getReturnTypeElementLua();
//    ChooseTypeExpression expr = new ChooseTypeExpression(constraints, PsiManager.getInstance(project));
//    TemplateBuilderImpl builder = new TemplateBuilderImpl(method);
//    if (!isConstructor) {
//      assert typeElement != null;
//      builder.replaceElement(typeElement, expr);
//    }
//    GrParameter[] parameters = method.getParameterList().getLuaParameters();
//    assert parameters.length == argTypes.length;
//    for (int i = 0; i < parameters.length; i++) {
//      GrParameter parameter = parameters[i];
//      GrTypeElement parameterTypeElement = parameter.getTypeElementLua();
//      builder.replaceElement(parameterTypeElement, paramTypesExpressions[i]);
//      builder.replaceElement(parameter.getNameIdentifierLua(), new ParameterNameExpression());
//    }
//    GrOpenBlock body = method.getBlock();
//    assert body != null;
//    PsiElement lbrace = body.getLBrace();
//    assert lbrace != null;
//    builder.setEndVariableAfter(lbrace);
//
//    method = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(method);
//    Template template = builder.buildTemplate();
//
//    Editor newEditor = QuickfixUtil.positionCursor(project, owner.getContainingFile(), method);
//    TextRange range = method.getTextRange();
//    newEditor.getDocument().deleteString(range.getStartOffset(), range.getEndOffset());
//
//    TemplateManager manager = TemplateManager.getInstance(project);
//    manager.startTemplate(newEditor, template);
//  }
}
