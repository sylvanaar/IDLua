/*
* Copyright 2000-2005 JetBrains s.r.o.
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
package com.sylvanaar.idea.Lua.lang.psi.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author max
 */
public class ResolveUtil {
    private ResolveUtil() {
    }

    public static final PsiScopeProcessor.Event DECLARATION_SCOPE_PASSED = new PsiScopeProcessor.Event() {
    };


    @Nullable
    public static PsiElement treeWalkUp(PsiScopeProcessor processor, PsiElement elt, PsiElement lastParent, PsiElement place) {
        if (elt == null) return null;

        PsiElement cur = elt;
        do {
            if (!cur.processDeclarations(processor, ResolveState.initial(), cur == elt ? lastParent : null, place)) {
                if (processor instanceof ResolveProcessor) {
                    return ((ResolveProcessor) processor).getResult();
                }
            }
            if (cur instanceof PsiFile) break;
//        if (cur instanceof LuaStatement && cur.getContext() instanceof JSIfStatement) {
//          // Do not try to resolve variables from then branch in else branch.
//          break;
//        }


            cur = cur.getPrevSibling();
        } while (cur != null);

//        final PsiElement func = processFunctionDeclarations(processor, elt.getContext());
//        if (func != null) return func;

        return treeWalkUp(processor, elt.getContext(), elt, place);
    }

    public static boolean processChildren(PsiElement element,
                                          PsiScopeProcessor processor,
                                          ResolveState substitutor,
                                          PsiElement lastParent,
                                          PsiElement place) {
        PsiElement run = lastParent == null ? element.getLastChild() : lastParent.getPrevSibling();
        while (run != null) {
            if (!run.processDeclarations(processor, substitutor, null, place)) return false;
            run = run.getPrevSibling();
        }

        return true;
    }

    
    
//    @Nullable
//    private static PsiElement processFunctionDeclarations(final @NotNull PsiScopeProcessor processor, final @Nullable PsiElement context) {
//        if (context != null) {
//            PsiElement cur = context.getLastChild();
//            while (cur != null) {
//                if (cur instanceof LuaFunctionDefinitionStatement) {
//                    if (!processor.execute(cur, ResolveState.initial())) {
//                        if (processor instanceof ResolveProcessor) {
//                            return ((ResolveProcessor) processor).getResult();
//                        }
//                    }
//                }
//                cur = cur.getPrevSibling();
//            }
//        }
//        return null;
//    }

    public static class ResolveProcessor implements PsiScopeProcessor {
        private static final Logger log = Logger.getInstance("#ResolveProcessor");
        private String myName;
        private PsiElement myResult = null;

        public ResolveProcessor(final String name) {
            myName = name;
        }

        public PsiElement getResult() {
            return myResult;
        }

        public boolean execute(PsiElement element, ResolveState stat) {

            if (element instanceof PsiNamedElement &&
                    element instanceof LuaPsiElement
                    ) {


                log.info("resolving " + myName + " checking " + element);
                if (myName.equals(((PsiNamedElement) element).getName())) {
                    myResult = element;

                    log.info("resolved to " + element);
                    return false;
                }
            }

            return true;
        }

        public <T> T getHint(Key<T> hintClass) {
            return null;
        }

        public void handleEvent(Event event, Object associated) {
            log.info("handle event " + event + " object " + associated);
        }
    }

    public static class VariantsProcessor implements PsiScopeProcessor {
        private List<PsiElement> myNames = new ArrayList<PsiElement>();

        public VariantsProcessor() {
        }

        public PsiElement[] getResult() {
            return myNames.toArray(new PsiElement[myNames.size()]);
        }

        public boolean execute(PsiElement element, ResolveState state) {
            if (element instanceof PsiNamedElement &&
                    element instanceof LuaPsiElement
                    ) {
                myNames.add(element);
            }

            return true;
        }


        public <T> T getHint(Key<T> hintClass) {
            return null;
        }

        public void handleEvent(Event event, Object associated) {
        }
    }
}


