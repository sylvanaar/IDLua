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

package com.sylvanaar.idea.Lua.lang.psi.util;


import com.intellij.psi.PsiElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.containers.MultiMap;

import java.util.Iterator;

/**
 * The base class for psi processors.
 * <p/>
 * Date: 14.04.2009
 * Time: 17:30:27
 *
 * @author Joachim Ansorg
 */
public abstract class LuaAbstractProcessor implements PsiScopeProcessor, ResolveProcessor {
    private final MultiMap<Integer, PsiElement> results = new MultiMap<Integer,PsiElement>();

    public void handleEvent(Event event, Object o) {
    }

    public final PsiElement getBestResult(boolean firstResult, PsiElement referenceElement) {
        return findBestResult(results, firstResult, referenceElement);
    }

    public Iterable<?> getResults() {
        return results.values();
    }

    public boolean hasResults() {
        return !results.isEmpty();
    }

    protected final void storeResult(PsiElement element, Integer rating) {
        results.putValue(rating, element);
    }

    /**
     * Returns the best results. It takes all the elements which have been rated the best
     * and returns the first / last, depending on the parameter.
     *
     * @param results          The results to check
     * @param firstResult      If the first element of the best element list should be returned.
     * @param referenceElement
     * @return The result
     */
    private PsiElement findBestResult(MultiMap<Integer, PsiElement> results, boolean firstResult, PsiElement referenceElement) {
        if (results.isEmpty()) {
            return null;
        }

        if (firstResult) {
            return results.values().iterator().next();
        }

        //if the first should not be used return the best element
        int referenceLevel = referenceElement != null ? LuaPsiUtils.blockNestingLevel(referenceElement) : 0;

        //find the best suitable result rating
        // The best one is as close as possible to the given referenceElement
        int bestRating = Integer.MAX_VALUE;
        int bestDelta = Integer.MAX_VALUE;
        for (int rating : results.keySet()) {
            final int delta = Math.abs(referenceLevel - rating);
            if (delta < bestDelta) {
                bestDelta = delta;
                bestRating = rating;
            }
        }

        //now get the correct result
        Iterator<PsiElement> i = results.get(bestRating).iterator();

        PsiElement v=null;
        while(i.hasNext()) v = i.next();

        return v;
    }

    public void reset() {
        results.clear();
    }
}
