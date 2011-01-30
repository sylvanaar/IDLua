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

package com.sylvanaar.idea.Lua.lang.psi.types;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/29/11
 * Time: 6:59 PM
 */
public class LuaPsiType extends PsiType {
    public static final LuaPsiType DYNAMIC = new LuaPsiType(null);

    protected LuaPsiType(@org.jetbrains.annotations.NotNull PsiAnnotation[] annotations) {
        super(annotations);
    }

    @Override
    public String getPresentableText() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getCanonicalText() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getInternalCanonicalText() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isValid() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean equalsToText(@NonNls String text) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <A> A accept(PsiTypeVisitor<A> visitor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GlobalSearchScope getResolveScope() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public PsiType[] getSuperTypes() {
        return new PsiType[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
