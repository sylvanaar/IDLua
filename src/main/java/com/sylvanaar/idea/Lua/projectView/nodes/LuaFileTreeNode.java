/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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
package com.sylvanaar.idea.Lua.projectView.nodes;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.projectView.LuaPsiFileChildrenSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LuaFileTreeNode extends PsiFileNode {
    public LuaFileTreeNode(Project project, LuaPsiFile value, ViewSettings viewSettings) {
        super(project, value, viewSettings);
    }

    @Override
    public Collection<AbstractTreeNode> getChildrenImpl() {
        PsiFile parent = getValue();
        final Collection<AbstractTreeNode> treeNodes = new ArrayList<AbstractTreeNode>();

        if (parent instanceof LuaPsiFile && getSettings().isShowMembers()) {
            List<PsiElement> result = new ArrayList<PsiElement>();

            LuaPsiFileChildrenSource.DEFAULT_CHILDREN.addChildren((LuaPsiFile) parent, result);

            for (PsiElement psiElement : result) {
                if (!psiElement.isPhysical()) {
                    continue;
                }

                if (psiElement instanceof LuaFunctionDefinition) {
                    treeNodes.add(new LuaFunctionNode(getProject(), (LuaFunctionDefinition) psiElement, getSettings()));
                }
            }
        }
        return treeNodes;
    }

    @Override
    public boolean isAlwaysLeaf() {
        return !getSettings().isShowMembers();
    }

    @Override
    public boolean expandOnDoubleClick() {
        return false;
    }

    @Override
    public boolean shouldDrillDownOnEmptyElement() {
        return false;
    }
}
