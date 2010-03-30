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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiQualifiedReference;
import com.intellij.psi.PsiType;
import com.sylvanaar.idea.Lua.psi.types.LuaTypeArgumentList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



/**
 * @author ven
 */
public interface LuaReferenceElement extends LuaElement, PsiPolyVariantReference, PsiQualifiedReference {
  @Nullable
  String getReferenceName();

  @Nullable
  PsiElement getReferenceNameElement();

  PsiElement resolve();

 LuaResolveResult advancedResolve();

  @NotNull
 LuaResolveResult[] multiResolve(boolean incompleteCode);

  @NotNull
  PsiType[] getTypeArguments();

  @Nullable
  LuaTypeArgumentList getTypeArgumentList();
}