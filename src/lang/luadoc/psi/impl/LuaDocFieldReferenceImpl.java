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
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocFieldReference;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;




public class LuaDocFieldReferenceImpl extends LuaDocMemberReferenceImpl implements LuaDocFieldReference {

  public LuaDocFieldReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "LuaDocFieldReference";
  }

  public void accept(LuaElementVisitor visitor) {
    visitor.visitDocFieldReference(this);
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
//    final PsiElement resolved = resolve();
//    if (resolved instanceof PsiMethod) {
//      final PsiMethod method = (PsiMethod) resolved;
//      final String oldName = getReferenceName();
//      if (!method.getName().equals(oldName)) { //was property reference to accessor
//        if (PropertyUtil.isSimplePropertyAccessor(method)) {
//          final String newPropertyName = PropertyUtil.getPropertyName(newElementName);
//          if (newPropertyName != null) {
//            return super.handleElementRename(newPropertyName);
//          }
//        }
//      }
//    } else if (resolved instanceof LuaField && ((LuaField) resolved).isProperty()) {
//      final LuaField field = (LuaField) resolved;
//      final String oldName = getReferenceName();
//      if (oldName != null && oldName.equals(field.getName())) {
//        if (oldName.startsWith("get")) {
//          return super.handleElementRename("get" + StringUtil.capitalize(newElementName));
//        } else if (oldName.startsWith("set")) {
//          return super.handleElementRename("set" + StringUtil.capitalize(newElementName));
//        }
//      }
//    }

    return super.handleElementRename(newElementName);
  }

  protected ResolveResult[] multiResolveImpl() {
    String name = getReferenceName();
//    LuaDocReferenceElement holder = getReferenceHolder();
//    PsiElement resolved;
//    if (holder != null) {
//      LuaCodeReferenceElement referenceElement = holder.getReferenceElement();
//      resolved = referenceElement != null ? referenceElement.resolve() : null;
//    } else {
//      resolved = getEnclosingClass(this);
//    }
//    if (resolved instanceof PsiClass) {
//      PropertyResolverProcessor processor = new PropertyResolverProcessor(name, this);
//      resolved.processDeclarations(processor, ResolveState.initial(), resolved, this);
//      LuaResolveResult[] candidates = processor.getCandidates();
//      if (candidates.length == 0) {
//        PsiType thisType = JavaPsiFacade.getInstance(getProject()).getElementFactory().createType((PsiClass) resolved, PsiSubstitutor.EMPTY);
//        MethodResolverProcessor methodProcessor = new MethodResolverProcessor(name, this, false, thisType, null, PsiType.EMPTY_ARRAY);
//        MethodResolverProcessor constructorProcessor = new MethodResolverProcessor(name, this, true, thisType, null, PsiType.EMPTY_ARRAY);
//        resolved.processDeclarations(methodProcessor, ResolveState.initial(), resolved, this);
//        resolved.processDeclarations(constructorProcessor, ResolveState.initial(), resolved, this);
//        candidates = ArrayUtil.mergeArrays(methodProcessor.getCandidates(), constructorProcessor.getCandidates(), LuaResolveResult.class);
//        if (candidates.length > 0) {
//          candidates = new LuaResolveResult[]{candidates[0]};
//        }
//      }
//      return candidates;
//    }
    return new ResolveResult[0];
  }

}
