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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocSymbolReference;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaCompoundReferenceElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResultImpl;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobal;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;

import java.util.HashSet;
import java.util.Set;


/**
 * @author ilyas
 */
public class SymbolResolveProcessor extends ResolveProcessor {
    private static final Logger log = Logger.getInstance("Lua.SymbolResolver");

    private final Set<PsiElement> myProcessedElements = new HashSet<PsiElement>();
    private final PsiElement myPlace;
    private final boolean    incompleteCode;


    public SymbolResolveProcessor(String myName, PsiElement myPlace, boolean incompleteCode) {
        super(myName);
        this.myPlace = myPlace;
        this.incompleteCode = incompleteCode;

        log.debug("---- Resolving: " + myName + " ----");
        log.debug("place: " + myPlace );
    }

    public SymbolResolveProcessor(LuaReferenceElement ref, boolean incompleteCode) {
        this(ref.getCanonicalText(), ref, incompleteCode);
    }

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    private boolean filter = true;


    public boolean execute(PsiElement element, ResolveState resolveState) {

        if (element instanceof LuaNamedElement && !myProcessedElements.contains(element)) {
            String resolvedName = getNameToResolve((LuaNamedElement) element);
            if (log.isDebugEnabled()) log.debug("Resolve: CHECK " + myName + " -> " + resolvedName);
            LuaNamedElement namedElement = (LuaNamedElement) element;
            boolean isAccessible = isAccessible(namedElement);
            if (!filter || isAccessible) {
                if (!PsiTreeUtil.hasErrorElements(namedElement)) {
                if (log.isDebugEnabled()) log.debug("Resolve: MATCH " + element.toString());
                myCandidates.add(new LuaResolveResultImpl(namedElement, true));
                }
            }
            myProcessedElements.add(namedElement);
            return !filter || !isAccessible || ((PsiReference) myPlace).getElement() instanceof LuaGlobal;
        }


        return true;
    }

    /*
   todo: add ElementClassHints
    */
    public <T> T getHint(Key<T> hintKey) {
//    if (hintKey == NameHint.KEY && myName != null) {
//      return (T) this;
//    }

        return null;
    }

    public PsiElement getPlace() {
        return myPlace;
    }

    public String getName(ResolveState resolveState) {
        return myName;
    }

//  public boolean shouldProcess(DeclaractionKind kind) {
//    return true;
//  }

    protected boolean isAccessible(LuaNamedElement namedElement) {
        if (myName == null) return true;

        String elementName = getNameToResolve(namedElement);

        if (myPlace instanceof LuaCompoundReferenceElementImpl) {
            return myName.equals(elementName);
        } else if (myPlace instanceof LuaDocSymbolReference) {
            return myName.equals(elementName);
        } else if (myPlace instanceof LuaReferenceElement) {
            final PsiElement element = ((LuaReferenceElement) myPlace).getElement();
            if (element instanceof LuaSymbol && namedElement instanceof LuaSymbol)
                return (myName.equals(elementName) && ((LuaSymbol) namedElement).isSameKind((LuaSymbol) element));
        }

        return myName.equals(elementName);
    }

    private String getNameToResolve(LuaNamedElement namedElement) {
        return namedElement instanceof LuaGlobal ? ((LuaGlobal) namedElement).getGlobalEnvironmentName() :
                namedElement
                .getName();
    }
}
