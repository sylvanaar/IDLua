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

package com.sylvanaar.idea.Lua.codeInsight;

import com.intellij.codeInsight.TargetElementEvaluator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaLocalDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocal;
import org.jetbrains.annotations.NotNull;


public class LuaTargetElementEvaluator implements TargetElementEvaluator {
  public static final Logger log = Logger.getInstance("#Lua.LuaTargetElementEvaluator");

  public boolean includeSelfInGotoImplementation(@NotNull PsiElement element) {
    return true;
  }

  public PsiElement getElementByReference(PsiReference ref, int flags) {
    log.info("target: " + ref);

   // PsiElement sourceElement = ref.getElement();

    if (ref instanceof LuaReferenceElement) {
      PsiElement resolved = ((LuaReferenceElement)ref).resolve();
      log.info("result: " + resolved);


      if (resolved instanceof LuaLocal && resolved.getContext().getContext() instanceof LuaLocalDefinitionStatement) {
          LuaLocalDefinitionStatement stat = (LuaLocalDefinitionStatement) resolved.getContext().getContext();

          LuaExpression[] exprs = stat.getExprs();

          if (exprs.length > 0) {
              LuaExpression expr = exprs[0];

              if (expr instanceof LuaReferenceElement) {
                  PsiElement resolved2 = ((LuaReferenceElement) expr).resolve();

                  if (resolved != null)
                      resolved = resolved2;
              }
          }


      }
      return resolved;
    }

    return null;
  }
}
