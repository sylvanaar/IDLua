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

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.util.SymbolUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class LuaFunctionNode extends BasePsiMemberNode<LuaFunctionDefinition> {
    public LuaFunctionNode(Project project, LuaFunctionDefinition value, ViewSettings viewSettings) {
        super(project, value, viewSettings);
    }

    public Collection<AbstractTreeNode> getChildrenImpl() {
        return null;
    }

    @Override
    protected void updateImpl(PresentationData data) {
        data.setPresentableText(StringUtil.notNullize(getFunctionName(getValue()), "<anon>"));
    }

    @Nullable
    String getFunctionName(LuaFunctionDefinition f) {
        if (f == null) return null;
        LuaSymbol i = f.getIdentifier();
        if (i == null) return null;
        final String name = SymbolUtil.getFullSymbolName(i);
        if (name == null) return null;
        return name;
    }

    @Override
    public boolean isAlwaysLeaf() {
        return true;
    }

    @Override
    public String getTitle() {
        final LuaFunctionDefinition function = getValue();
        return function != null ? StringUtil.notNullize(getFunctionName(function), "<anon>") : super.getTitle();
    }
}
