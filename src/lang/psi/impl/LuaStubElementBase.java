/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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
package com.sylvanaar.idea.Lua.lang.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author ilyas
 */
public abstract class LuaStubElementBase<T extends StubElement> extends StubBasedPsiElementBase<T> implements
    LuaPsiElement {

  protected LuaStubElementBase(final T stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public LuaStubElementBase(final ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement getParent() {
      return getParentByStub();
  }

  public void accept(LuaElementVisitor visitor) {
    visitor.visitElement(this);
  }

  public void acceptChildren(LuaElementVisitor visitor) {
    LuaPsiElementImpl.acceptLuaChildren(this, visitor);
  }

  protected PsiElement getDefinitionParent() {
    final PsiElement candidate = getParentByStub();
    if (candidate instanceof LuaPsiFile) {
      return candidate;
    }

    return SharedImplUtil.getParent(getNode());
  }


  public String getPresentationText() {
    return getText();
  }

  @Override
  public ItemPresentation getPresentation() {
    return new ItemPresentation() {
      public String getPresentableText() {
        return getPresentationText();
      }

      @Nullable
      public String getLocationString() {
        String name = getContainingFile().getName();
        return "(in " + name + ")";
      }

      @Nullable
      public Icon getIcon(boolean open) {
        return LuaIcons.LUA_ICON;
      }

      @Nullable
      public TextAttributesKey getTextAttributesKey() {
        return null;
      }
    };
  }

}