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

package com.sylvanaar.idea.Lua.lang.psi.resolve.processors;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.NameHint;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResultImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ilyas
 */
public class SymbolResolveProcessor extends ResolveProcessor implements NameHint {

  private final Set<PsiElement> myProcessedElements = new HashSet<PsiElement>();
  private final PsiElement myPlace;
  private final boolean incompleteCode;
  private final boolean onlyJava;

  public SymbolResolveProcessor(String myName, PsiElement myPlace, boolean incompleteCode, boolean onlyJava) {
    super(myName);
    this.myPlace = myPlace;
    this.incompleteCode = incompleteCode;
    this.onlyJava = onlyJava;
  }


  public boolean execute(PsiElement element, ResolveState resolveState) {
    // todo add resolve kinds
    if (onlyJava && !(element instanceof PsiClass)) return true;
    if (element instanceof PsiNamedElement && !myProcessedElements.contains(element)) {
      PsiNamedElement namedElement = (PsiNamedElement) element;
      boolean isAccessible = isAccessible(namedElement);
      myCandidates.add(new LuaResolveResultImpl(namedElement, isAccessible));
      myProcessedElements.add(namedElement);
      return true; // TODO !ListDeclarations.isLocal(element);
      //todo specify as it's possible!
    }

    return true;
  }

  /*
  todo: add ElementClassHints
   */
  public <T> T getHint(Key<T> hintKey) {
    if (hintKey == NameHint.KEY && myName != null) {
      return (T) this;
    }

    return null;
  }

  public PsiElement getPlace() {
    return myPlace;
  }

  public String getName(ResolveState resolveState) {
    return myName;
  }

  public boolean shouldProcess(DeclaractionKind kind) {
    return true;
  }

  protected boolean isAccessible(PsiNamedElement namedElement) {
    //todo implement me
    return true;
  }
}
