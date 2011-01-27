/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.resolve;

import com.intellij.psi.PsiElement;

/**
 * @author ilyas
 */
public class LuaResolveResultImpl implements LuaResolveResult {

  private  final PsiElement myElement;
  private final boolean myIsAccessible;

  public LuaResolveResultImpl(PsiElement myElement, boolean myIsAccessible) {
    this.myElement = myElement;
    this.myIsAccessible = myIsAccessible;
  }


  public PsiElement getElement() {
    return myElement;
  }

  public boolean isValidResult() {
    return isAccessible();
  }
  
  public boolean isAccessible() {
    return myIsAccessible;
  }


}
