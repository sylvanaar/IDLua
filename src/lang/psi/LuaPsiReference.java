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

package com.sylvanaar.idea.Lua.lang.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.util.IncorrectOperationException;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Aug 25, 2010
 * Time: 7:40:39 AM
 */
public interface LuaPsiReference
{
    public abstract LuaPsiElement getElement();

    public abstract TextRange getRangeInElement();

    public abstract LuaPsiElement resolve();

    public abstract String getCanonicalText();

    public abstract LuaPsiElement handleElementRename(String s)
        throws IncorrectOperationException;

    public abstract LuaPsiElement bindToElement(LuaPsiElement psielement)
        throws IncorrectOperationException;

    public abstract boolean isReferenceTo(LuaPsiElement psielement);

    public abstract Object[] getVariants();

    public abstract boolean isSoft();
}