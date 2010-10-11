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

package com.sylvanaar.idea.Lua.lang.psi.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiPolyVariantReference;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;


/**
 * @author ilyas
 */
public interface LuaReferenceExpression extends LuaExpression, LuaReferenceElement, LuaNamedElement, PsiPolyVariantReference {

    public ASTNode getNameElement();
//  @Nullable
//  LuaExpression getQualifierExpression();
//
//  boolean isQualified();
//
//  @Nullable
//  IElementType getDotTokenType();
//
//  @Nullable
//  PsiElement getDotToken();
//
//  void replaceDotToken(PsiElement newDotToken);
//
//  //not caching!
//  @NotNull
//  LuaResolveResult[] getSameNameVariants();
//
//  void setQualifierExpression(LuaReferenceExpression qualifierExpression);
//
//  LuaReferenceExpression bindToElementViaStaticImport(@NotNull PsiClass qualifierClass);
}