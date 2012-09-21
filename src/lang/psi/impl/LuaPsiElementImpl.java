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

package com.sylvanaar.idea.Lua.lang.psi.impl;

import com.intellij.extapi.psi.*;
import com.intellij.lang.*;
import com.intellij.openapi.diagnostic.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.*;
import com.intellij.psi.impl.source.*;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.psi.search.*;
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public class LuaPsiElementImpl extends ASTWrapperPsiElement implements LuaPsiElement  {
    private static final Logger log = Logger.getInstance("Lua.LuaPsiElementImpl");


    public LuaPsiElementImpl(ASTNode node) {
        super(node);
    }

    protected void log(String text) {
        log.debug(this + ": " + text);
    }

    @Override
    public Icon getIcon(int flags) {
        return LuaIcons.LUA_ICON;
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitElement(this);
    }

    @Override
    public void acceptChildren(LuaElementVisitor visitor) {
        acceptLuaChildren(this, visitor);
    }

    @Override
    public String getPresentationText() {
        return null;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitElement(this);
        } else {
            visitor.visitElement(this);
        }
    }

    public String toString() {
        return getNode().getElementType().toString();
    }

    @NotNull
    public Language getLanguage() {
        return LuaFileType.LUA_LANGUAGE;
    }

    @NotNull
    public SearchScope getUseScope() {
        //This is true as long as we have no inter-file references
        return new ProjectAndLibrariesScope(getProject());
    }

//    @Override
//    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
// PsiElement lastParent, @NotNull PsiElement place) {
//        return LuaPsiUtils.processChildDeclarations(this, processor, state, lastParent, place);
//    }

    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        CompositeElement treeElement = calcTreeElement();
        assert treeElement.getTreeParent() != null;
        CheckUtil.checkWritable(this);
        TreeElement elementCopy = ChangeUtil.copyToElement(newElement);
        treeElement.getTreeParent().replaceChildInternal(treeElement, elementCopy);
        elementCopy = ChangeUtil.decodeInformation(elementCopy);
        return SourceTreeToPsiMap.treeElementToPsi(elementCopy);
    }

    protected CompositeElement calcTreeElement() {
        return (CompositeElement) getNode();
    }


    public static void acceptLuaChildren(PsiElement parent, LuaElementVisitor visitor) {
        PsiElement child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof LuaPsiElement) {
                ((LuaPsiElement) child).accept(visitor);
            }

            child = child.getNextSibling();
        }
    }
    
    
}
