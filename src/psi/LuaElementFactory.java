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

package com.sylvanaar.idea.Lua.psi;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.psi.statements.LuaBlockStatement;
import com.sylvanaar.idea.Lua.psi.statements.LuaRequireStatement;
import com.sylvanaar.idea.Lua.psi.statements.LuaStatement;
import com.sylvanaar.idea.Lua.psi.statements.LuaVariableDeclaration;
import com.sylvanaar.idea.Lua.psi.statements.arguments.LuaArgumentList;
import com.sylvanaar.idea.Lua.psi.statements.arguments.LuaNamedArgument;
import com.sylvanaar.idea.Lua.psi.statements.blocks.LuaClosableBlock;
import com.sylvanaar.idea.Lua.psi.statements.blocks.LuaCodeBlock;
import com.sylvanaar.idea.Lua.psi.statements.expressions.LuaApplicationStatement;
import com.sylvanaar.idea.Lua.psi.statements.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.psi.statements.expressions.LuaParenthesizedExpression;
import com.sylvanaar.idea.Lua.psi.statements.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.psi.statements.expressions.path.LuaMethodCallExpression;
import com.sylvanaar.idea.Lua.psi.statements.params.LuaParameter;
import com.sylvanaar.idea.Lua.psi.statements.typedef.members.LuaEnumConstant;
import com.sylvanaar.idea.Lua.psi.statements.typedef.members.LuaMethod;
import com.sylvanaar.idea.Lua.psi.toplevel.LuaTopStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class LuaElementFactory {

  public static LuaElementFactory getInstance(Project project) {
    return ServiceManager.getService(project,LuaElementFactory.class);
  }

  /**
   * @param qName
   * @param isStatic
   * @param isOnDemand
   * @param alias
   * @return import statement for given class
   */
  public abstract LuaRequireStatement createImportStatementFromText(String qName, boolean isStatic, boolean isOnDemand, String alias);

  public abstract LuaRequireStatement createImportStatementFromText(String text);

  public abstract PsiElement createWhiteSpace();

  @NotNull
  public abstract PsiElement createLineTerminator(int length);

  public abstract LuaArgumentList createExpressionArgumentList(LuaExpression... expressions);

  public abstract LuaNamedArgument createNamedArgument(String name,LuaExpression expression);

  public abstract LuaStatement createStatementFromText(String text);

  public abstract LuaBlockStatement createBlockStatement(LuaStatement... statements);

  public abstract LuaMethodCallExpression createMethodCallByAppCall(LuaApplicationStatement callExpr);

  public abstract LuaReferenceExpression createReferenceExpressionFromText(String exprText);

 // public abstract LuaCodeReferenceElement createReferenceElementFromText(String refName);

  public abstract LuaExpression createExpressionFromText(String exprText);

  public abstract LuaVariableDeclaration createFieldDeclaration(String[] modifiers, String identifier,LuaExpression initializer, PsiType type);
  public abstract LuaVariableDeclaration createFieldDeclarationFromText(String text);

  public abstract LuaVariableDeclaration createVariableDeclaration(String[] modifiers,LuaExpression initializer, PsiType type, String... identifiers);

  public abstract LuaEnumConstant createEnumConstantFromText(String text);

  @NotNull
  public abstract PsiElement createReferenceNameFromText(String idText);

  public abstract PsiElement createDocMemberReferenceNameFromText(String idText);

  public abstract LuaTopStatement createTopElementFromText(String text);

  public abstract LuaClosableBlock createClosureFromText(String s) throws IncorrectOperationException;

  public abstract LuaParameter createParameter(String name, @Nullable String typeText,LuaElement context) throws IncorrectOperationException;

  //public abstract LuaCodeReferenceElement createTypeOrPackageReference(String qName);

  //public abstract LuaTypeDefinition createTypeDefinition(String text) throws IncorrectOperationException;

  //public abstract LuaTypeElement createTypeElement(PsiType type) throws IncorrectOperationException;

  @NotNull
  //public abstract LuaTypeElement createTypeElement(String typeText) throws IncorrectOperationException;

  public abstract LuaParenthesizedExpression createParenthesizedExpr(LuaExpression newExpr);

  public abstract PsiElement createStringLiteral(String text);

  public abstract PsiElement createModifierFromText(String name);

  public abstract LuaCodeBlock createMethodBodyFromText(String text);

  public abstract LuaVariableDeclaration createSimpleVariableDeclaration(String name, String typeText);

  public abstract LuaReferenceElement createPackageReferenceElementFromText(String newPackageName);

  public abstract PsiElement createDotToken(String newDot);

  public abstract LuaMethod createMethodFromText(String methodText);

  //public abstract LuaAnnotation createAnnotationFromText(String annoText);

  public abstract LuaFile createGroovyFile(String text, boolean isPhisical, PsiElement context);

  public abstract LuaMethod createMethodFromText(String modifier, String name, String type, String[] paramTypes);

  public abstract LuaMethod createConstructorFromText(@NotNull String constructorName, String[] paramTypes, String[] paramNames, String body);

//  public abstract LuaLabel createLabel(@NotNull String name);
}
