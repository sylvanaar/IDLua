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
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.search.GlobalSearchScope;
import com.sylvanaar.idea.Lua.lang.luadoc.lexer.LuaDocTokenTypes;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocMethodParameter;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocMethodParams;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author ilyas
 */
public class LuaDocMethodParamsImpl extends LuaDocPsiElementImpl implements LuaDocMethodParams {



  public LuaDocMethodParamsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "LuaDocMethodParameterList";
  }

  public void accept(LuaElementVisitor visitor) {
    visitor.visitDocMethodParameterList(this);
  }

  public PsiType[] getParameterTypes() {
    ArrayList<PsiType> types = new ArrayList<PsiType>();
    PsiManagerEx manager = getManager();
    GlobalSearchScope scope = GlobalSearchScope.allScope(getProject());
    PsiElementFactory factory = JavaPsiFacade.getInstance(getProject()).getElementFactory();
    for (LuaDocMethodParameter parameter : getParameters()) {
//      LuaDocReferenceElement typeElement = parameter.getTypeElement();
//      try {
//        PsiType type = factory.createTypeFromText(typeElement.getText(), this);
//        type = TypesUtil.boxPrimitiveType(type, manager, scope);
//        types.add(type);
//      } catch (IncorrectOperationException e) {
//        LOG.info(e);
//        types.add(null);
//      }
    }
    return types.toArray(new PsiType[types.size()]);
  }

  public LuaDocMethodParameter[] getParameters() {
    return findChildrenByClass(LuaDocMethodParameter.class);
  }

  @NotNull
  public PsiElement getLeftParen() {
    ASTNode paren = getNode().findChildByType(LuaDocTokenTypes.LDOC_TAG_VALUE_LPAREN);
    assert paren != null;
    return paren.getPsi();
  }

  @Nullable
  public PsiElement getRightParen() {
    ASTNode paren = getNode().findChildByType(LuaDocTokenTypes.LDOC_TAG_VALUE_RPAREN);
    return paren != null ? paren.getPsi() : null;
  }

}
