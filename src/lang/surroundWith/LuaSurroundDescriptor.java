/*
 * Copyright 2014 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.surroundWith;

import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Jon on 9/21/2014.
 */
public class LuaSurroundDescriptor implements SurroundDescriptor {
    private static final Surrounder[] ourSurrounders = new Surrounder[]{
    };

    @Override
    @NotNull
    public Surrounder[] getSurrounders() {
        return ourSurrounders;
    }

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    @NotNull
    public PsiElement[] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
        return PsiElement.EMPTY_ARRAY;
    }

}
