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
package com.sylvanaar.idea.Lua.lang.structure.itemsPresentations;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


public abstract class LuaItemPresentation implements ItemPresentation {
  protected final PsiElement myElement;

  protected LuaItemPresentation(PsiElement myElement) {
    this.myElement = myElement;
  }

  @Nullable
    public String getLocationString() {
    return null;
  }

  @Nullable
    public Icon getIcon(boolean open) {
    return  myElement.getIcon(Iconable.ICON_FLAG_OPEN);
  }

  @Nullable
    public TextAttributesKey getTextAttributesKey() {
    return null;
  }
}
