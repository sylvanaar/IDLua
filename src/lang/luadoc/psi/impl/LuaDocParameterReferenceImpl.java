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

package com.sylvanaar.idea.Lua.lang.luadoc.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocParameterReference;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocTagValueToken;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElementFactory;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResult;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResultImpl;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author ilyas
 */
public class LuaDocParameterReferenceImpl extends LuaDocReferenceElementImpl implements LuaDocParameterReference {

  public LuaDocParameterReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "LuaDocParameterReference";
  }

  public PsiReference getReference() {
    return this;
  }

  @NotNull
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final String name = getName();
    if (name == null) return ResolveResult.EMPTY_ARRAY;
    ArrayList<LuaResolveResult> candidates = new ArrayList<LuaResolveResult>();

    final PsiElement owner = LuaDocCommentUtil.findDocOwner(this);
    if (owner instanceof LuaFunctionDefinition) {
      final LuaFunctionDefinition method = (LuaFunctionDefinition)owner;
      final LuaParameter[] parameters = method.getParameters().getLuaParameters();

      for (LuaParameter parameter : parameters) {
        if (name.equals(parameter.getName())) {
          candidates.add(new LuaResolveResultImpl(parameter, true));
        }
      }
      return candidates.toArray(new ResolveResult[candidates.size()]);
    }

    return ResolveResult.EMPTY_ARRAY;
  }

  public PsiElement getElement() {
    return this;
  }

  public TextRange getRangeInElement() {
    return new TextRange(0, getTextLength());
  }

  @Override
  public String getName() {
    return getReferenceNameElement().getText();
  }

  @Nullable
  public PsiElement resolve() {
    final ResolveResult[] results = multiResolve(false);
    if (results.length != 1) return null;
    return results[0].getElement();
  }

  @NotNull
  public String getCanonicalText() {
    return StringUtil.notNullize(getName());
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
      LuaDocParameterReference newParameterReference = LuaPsiElementFactory.getInstance(getProject()).createParameterDocMemberReferenceNameFromText(newElementName);
      assert newParameterReference != null;

      replace(newParameterReference);
      return this;
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    if (isReferenceTo(element)) return this;
    return null;
  }

  public boolean isReferenceTo(PsiElement element) {
    if (!(element instanceof LuaParameter)) return false;
    return getManager().areElementsEquivalent(element, resolve());
  }

  @NotNull
  public Object[] getVariants() {
    final PsiElement owner = LuaDocCommentUtil.findDocOwner(this);
    if (owner instanceof LuaFunctionDefinition) {
      return ((LuaFunctionDefinition)owner).getParameters().getLuaParameters();
    }
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public boolean isSoft() {
    return false;
  }

  @Nullable
  public LuaDocTagValueToken getReferenceNameElement() {
    LuaDocTagValueToken token = findChildByClass(LuaDocTagValueToken.class);
   
    return token;
  }
}
