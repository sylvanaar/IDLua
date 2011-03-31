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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocMemberReference;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocReferenceElement;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocTagValueToken;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class LuaDocMemberReferenceImpl extends LuaDocPsiElementImpl implements LuaDocMemberReference {
  public LuaDocMemberReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  public LuaDocReferenceElement getReferenceHolder() {
    return findChildByClass(LuaDocReferenceElement.class);
  }

  public boolean isReferenceTo(PsiElement element) {
    return getManager().areElementsEquivalent(element, resolve());
  }

  @Nullable
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    if (isReferenceTo(element)) return this;

//    if (element instanceof PsiClass) {
//      LuaDocReferenceElement holder = getReferenceHolder();
//      if (holder != null) {
//        return replace(holder.getReferenceElement().bindToElement(element).getParent());
//      }
//      LuaDocReferenceElement ref =
//        LuaPsiElementFactory.getInstance(getProject()).createDocReferenceElementFromFQN(((PsiClass)element).getQualifiedName());
//      return replace(ref);
//    }
//    else if (element instanceof PsiMember) {
//      PsiClass clazz = ((PsiMember)element).getContainingClass();
//      if (clazz == null) return null;
//      String qName = clazz.getQualifiedName();
//      String memberRefText;
//      if (element instanceof PsiField) {
//        memberRefText = ((PsiField)element).getName();
//      }
//      else if (element instanceof PsiMethod) {
//        PsiParameterList list = ((PsiMethod)element).getParameterList();
//        StringBuilder builder = new StringBuilder();
//        builder.append(((PsiMethod)element).getName()).append("(");
//        PsiParameter[] params = list.getParameters();
//        for (int i = 0; i < params.length; i++) {
//          PsiParameter parameter = params[i];
//          PsiType type = parameter.getType();
//          if (i > 0) builder.append(", ");
//          builder.append(type.getPresentableText());
//        }
//        builder.append(")");
//        memberRefText = builder.toString();
//      }
//      else {
//        return null;
//      }
//      LuaDocMemberReference ref = LuaPsiElementFactory.getInstance(getProject()).createDocMemberReferenceFromText(qName, memberRefText);
//      return replace(ref);
//    }
    return null;
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
//    PsiElement nameElement = getReferenceNameElement();
//    ASTNode node = nameElement.getNode();
//    ASTNode newNameNode = LuaPsiElementFactory.getInstance(getProject()).createDocMemberReferenceNameFromText(newElementName).getNode();
//    assert newNameNode != null && node != null;
//    node.getTreeParent().replaceChild(node, newNameNode);
    return this;
  }

  @NotNull
  public LuaDocTagValueToken getReferenceNameElement() {
    LuaDocTagValueToken token = findChildByClass(LuaDocTagValueToken.class);
    assert token != null;
    return token;
  }

  public PsiElement getElement() {
    return this;
  }

  public PsiReference getReference() {
    return this;
  }

  public TextRange getRangeInElement() {
    final PsiElement refNameElement = getReferenceNameElement();
    final int offsetInParent = refNameElement.getStartOffsetInParent();
    return new TextRange(offsetInParent, offsetInParent + refNameElement.getTextLength());
  }

  @NotNull
  public String getCanonicalText() {
    return getRangeInElement().substring(getElement().getText());
  }

  public boolean isSoft() {
    return false;
  }

  @Nullable
  public PsiElement getQualifier() {
    return getReferenceHolder();
  }

  @Nullable
  @NonNls
  public String getReferenceName() {
    return getReferenceNameElement().getText();
  }

  @Nullable
  public PsiElement resolve() {
    ResolveResult[] results = multiResolve(false);
    if (results.length == 1) {
      return results[0].getElement();
    }
    return null;
  }

  @NotNull
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    return multiResolveImpl();
  }

  @NotNull
  public Object[] getVariants() {
//    LuaDocReferenceElement holder = getReferenceHolder();
//    PsiElement resolved;
//    if (holder != null) {
//      LuaCodeReferenceElement referenceElement = holder.getReferenceElement();
//      resolved = referenceElement != null ? referenceElement.resolve() : null;
//    } else {
//      resolved = getEnclosingClass(this);
//    }
//    if (resolved instanceof PsiClass) {
//      ResolverProcessor propertyProcessor = CompletionProcessor.createPropertyCompletionProcessor(this);
//      resolved.processDeclarations(propertyProcessor, ResolveState.initial(), null, this);
//      PsiElement[] propertyCandidates = ResolveUtil.mapToElements(propertyProcessor.getCandidates());
//      ResolverProcessor methodProcessor = CompletionProcessor.createPropertyCompletionProcessor(this);
//
//      resolved.processDeclarations(methodProcessor, ResolveState.initial(), null, this);
//
//      PsiElement[] methodCandidates = ResolveUtil.mapToElements(methodProcessor.getCandidates());
//
//      PsiElement[] elements = ArrayUtil.mergeArrays(propertyCandidates, methodCandidates, PsiElement.class);
//
//      return ContainerUtil.map2Array(elements, new Function<PsiElement, Object>() {
//        public Object fun(PsiElement psiElement) {
//          LookupElement lookupItem = null;
//          if (psiElement instanceof PsiNamedElement) {
//            if (psiElement instanceof PsiMethod) {
//              lookupItem = new LookupItem<PsiElement>(psiElement, ((PsiMethod) psiElement).getName());
//            } else {
//              String string = ((PsiNamedElement) psiElement).getName();
//              lookupItem = new LookupItem<PsiElement>(psiElement, string == null ? "" : string);
//            }
//            lookupItem.putUserData(LookupItem.FORCE_SHOW_SIGNATURE_ATTR, true);
//          }
//          return lookupItem != null ? lookupItem : psiElement;
//        }
//      });
//    }
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

//  protected static PsiClass getEnclosingClass(PsiElement element) {
//    PsiElement parent = element.getParent();
//    while (parent != null) {
//      if (parent instanceof LuaTypeDefinition) return (PsiClass) parent;
//      if (parent instanceof LuaFile) return ((LuaFile) parent).getScriptClass();
//      parent = parent.getParent();
//    }
//    return null;
//  }

  protected abstract ResolveResult[] multiResolveImpl();

}
