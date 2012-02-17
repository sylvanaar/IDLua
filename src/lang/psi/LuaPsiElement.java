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

import com.intellij.lang.ASTNode;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;


public interface LuaPsiElement extends PsiElement, NavigationItem {
    LuaPsiElement[] EMPTY_ARRAY = new LuaPsiElement[0];
    
    @NotNull
    ASTNode getNode();

    void accept(LuaElementVisitor visitor);

    void acceptChildren(LuaElementVisitor visitor);

    String getPresentationText();

//    String getText();
//
//    LuaPsiElement replace(LuaPsiElement replacement);
//
//    LuaPsiElement getParent();
//
//    LuaPsiElement addBefore(LuaPsiElement replacement, LuaPsiElement original);
//
//    void delete();
}
