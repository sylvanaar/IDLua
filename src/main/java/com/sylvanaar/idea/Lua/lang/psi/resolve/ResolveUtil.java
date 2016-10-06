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

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.resolve.completion.CompletionProcessor;
import com.sylvanaar.idea.Lua.lang.psi.stubs.index.LuaGlobalDeclarationIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * @author ilyas
 */
public abstract class ResolveUtil {

  public static boolean treeWalkUp(PsiElement place, PsiScopeProcessor processor) {
    PsiElement lastParent = null;
    PsiElement run = place;
    while (run != null) {
      if (!run.processDeclarations(processor, ResolveState.initial(), lastParent, place)) return false;
      lastParent = run;
      run = run.getContext(); //same as getParent
    }

    return true;
  }

  public static boolean processChildren(PsiElement element, PsiScopeProcessor processor,
                                        ResolveState substitutor, PsiElement lastParent, PsiElement place) {
    PsiElement run = lastParent == null ? element.getLastChild() : lastParent.getPrevSibling();
    while (run != null) {
      if (PsiTreeUtil.findCommonParent(place, run) != run && !run.processDeclarations(processor, substitutor, lastParent, place)) return false;
      run = run.getPrevSibling();
    }

    return true;
  }

    @NotNull
    public static Object[] getVariants(LuaReferenceElement e) {
//        return new Object[0];
        CompletionProcessor variantsProcessor = new CompletionProcessor(e);
        ResolveUtil.treeWalkUp(e, variantsProcessor);
//
//        Collection<Object> names = new LinkedList<Object>();
//
//        names.addAll(LuaPsiManager.getInstance(e.getProject()).getFilteredGlobalsCache());
//        names.addAll(variantsProcessor.getResultCollection());

        return variantsProcessor.getResultElements();
    }


//  public static boolean processElement(PsiScopeProcessor processor, PsiNamedElement namedElement) {
//    if (namedElement == null) return true;
//    NameHint nameHint = processor.getHint(NameHint.KEY);
//    String name = nameHint == null ? null : nameHint.getName(ResolveState.initial());
//    if (name == null || name.equals(namedElement.getName())) {
//      return processor.execute(namedElement, ResolveState.initial());
//    }
//    return true;
//  }

  public static PsiElement[] mapToElements(LuaResolveResult[] candidates) {
    PsiElement[] elements = new PsiElement[candidates.length];
    for (int i = 0; i < elements.length; i++) {
      elements[i] = candidates[i].getElement();
    }

    return elements;
  }


    public static Collection<LuaDeclarationExpression> getFilteredGlobals(Project project, GlobalSearchScope scope) {
        LuaGlobalDeclarationIndex index = LuaGlobalDeclarationIndex.getInstance();
        HashSet<String> names = new HashSet<String>(index.getAllKeys(project));

//        Collection<String> rejects = new LinkedList<String>();
        Collection<LuaDeclarationExpression> exprs = new LinkedList<LuaDeclarationExpression>();
        for (String name : names) {
            Collection<LuaDeclarationExpression> elems = index.get(name, project, scope);
//            if (elems.size() == 0)
//                rejects.add(name);
//            else
                exprs.addAll(elems);
        }

//        names.removeAll(rejects);
        return exprs;
    }

}
