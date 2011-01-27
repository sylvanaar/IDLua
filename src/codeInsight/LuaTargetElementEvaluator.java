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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Medvedev
 */
public class LuaTargetElementEvaluator implements TargetElementEvaluator {
  public boolean includeSelfInGotoImplementation(@NotNull PsiElement element) {
    return true;
  }

  public PsiElement getElementByReference(PsiReference ref, int flags) {
    PsiElement sourceElement = ref.getElement();

//    if (sourceElement instanceof LuaReferenceElement) {
//      GrNewExpression newExpr;
//
//      if (sourceElement.getParent() instanceof GrNewExpression) {
//        newExpr = (GrNewExpression)sourceElement.getParent();
//      }
//      else if (sourceElement.getParent().getParent() instanceof GrNewExpression) {//anonymous class declaration
//        newExpr = (GrNewExpression)sourceElement.getParent().getParent();
//      }
//      else {
//        return null;
//      }
//
//      final PsiMethod constructor = newExpr.resolveMethod();
//      final GrArgumentList argumentList = newExpr.getArgumentList();
//      if (constructor != null &&
//          argumentList != null &&
//          argumentList.getNamedArguments().length != 0 &&
//          argumentList.getExpressionArguments().length == 0) {
//        if (constructor.getParameterList().getParametersCount() == 0) return constructor.getContainingClass();
//      }
//
//      return constructor;
//    }

    if (sourceElement instanceof LuaReferenceExpression) {
      PsiElement resolved = ((LuaReferenceExpression)sourceElement).resolve();
//       if (resolved instanceof GrGdkMethod) {
//        return correctSearchTargets(resolved);
//      }
      return resolved;
    }

    return null;
  }

//  @Nullable
//  public static PsiElement correctSearchTargets(@Nullable PsiElement target) {
//    if (target != null && !(target instanceof GrAccessorMethod) && !target.isPhysical()) {
//      return target.getNavigationElement();
//    }
//    return target;
//  }
}
