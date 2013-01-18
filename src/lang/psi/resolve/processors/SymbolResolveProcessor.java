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

import com.intellij.openapi.diagnostic.*;
import com.intellij.openapi.util.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.resolve.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import org.jetbrains.annotations.*;

import java.util.*;


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

//        log.debug("---- Resolving: " + myName + " ----");
//        log.debug("place: " + myPlace );
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


    public boolean execute(@NotNull PsiElement element, ResolveState resolveState) {

        if (element instanceof LuaNamedElement && !myProcessedElements.contains(element)) {
            String resolvedName = getNameToResolve((LuaNamedElement) element);
//            if (log.isDebugEnabled()) log.debug("Resolve: CHECK " + myName + " -> " + resolvedName);
            LuaNamedElement namedElement = (LuaNamedElement) element;
            boolean isAccessible = isAccessible(namedElement);
            if (!filter || isAccessible) {
                if ((namedElement instanceof StubBasedPsiElement &&
                     ((StubBasedPsiElement) namedElement).getStub() != null) ||
                    !PsiTreeUtil.hasErrorElements(namedElement)) {
//                if (log.isDebugEnabled()) log.debug("Resolve: MATCH " + element.toString());
                    addCandidate(namedElement);
                } else log.debug("Match fail: " + namedElement);
            }
            myProcessedElements.add(namedElement);
            return !filter || !isAccessible || ((PsiReference) myPlace).getElement() instanceof LuaGlobal;
        }


        return true;
    }

    private void addCandidate(LuaNamedElement namedElement) {
//        myFirstMatch = myFirstMatch == null ? namedElement : myFirstMatch;
//        myCandidates.add(new LuaResolveResultImpl(namedElement, true));
       addCandidate(new LuaResolveResultImpl(namedElement, true));
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

        if (myPlace instanceof LuaRequireExpression) {
            return  namedElement instanceof LuaModuleExpression && myName.equals(elementName);
        } else if (myPlace instanceof LuaCompoundReferenceElementImpl) {
            if (namedElement instanceof LuaCompoundIdentifier)
                if (((LuaCompoundIdentifier) namedElement).getEnclosingIdentifier() != namedElement)
                    return false;
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
