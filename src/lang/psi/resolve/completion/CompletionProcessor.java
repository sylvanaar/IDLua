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

package com.sylvanaar.idea.Lua.lang.psi.resolve.completion;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.sylvanaar.idea.Lua.lang.psi.resolve.processors.SymbolResolveProcessor;

/**
 * @author ilyas
 */
public class CompletionProcessor extends SymbolResolveProcessor {

  public CompletionProcessor(PsiElement myPlace) {
    super(null, myPlace, true);
  }


  public boolean execute(PsiElement element, ResolveState state) {
    super.execute(element, state);
    return true;
  }
}
