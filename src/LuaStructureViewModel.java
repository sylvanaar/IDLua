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

package com.sylvanaar.idea.Lua;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.psi.LuaPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 3:25:48 PM
 */
public class LuaStructureViewModel  extends TextEditorBasedStructureViewModel {
    LuaPsiElement myElement;


    protected LuaStructureViewModel(@org.jetbrains.annotations.NotNull PsiFile psiFile) {
        super(psiFile);
    }

    @NotNull
    @Override
    public StructureViewTreeElement getRoot() {
        return null;  
    }
}