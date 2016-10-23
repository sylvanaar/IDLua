/*
 * Copyright 2016 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;

public class LuaDebugVariable extends XNamedValue {
    private final LuaDebugVariable parent;
    private final LuaDebugValue value;
    private final boolean isIndex;
    private final boolean isLocal;

    LuaDebugVariable(String name, LuaDebugValue value, boolean isLocal) {
        this(name, null, value, false, isLocal);
    }

    private LuaDebugVariable(String name, LuaDebugVariable parent, LuaDebugValue value, boolean iaIndex, boolean
            isLocal) {
        super(name);
        this.parent = parent;
        this.value = value;
        this.isIndex = iaIndex;
        this.isLocal = isLocal;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (value.isTable()) {
            LuaTable myTable = value.getRawValue().checktable();
            final XValueChildrenList xValues = new XValueChildrenList(myTable.keyCount());
            for (LuaValue key : myTable.keys()) {
                if (key.equals(LuaRemoteStack.TABLE_ID_KEY)) continue;
                final LuaValue rawValue = myTable.get(key);
                final LuaDebugValue debugValue = new LuaDebugValue(rawValue, AllIcons.Nodes.Field);
                final LuaDebugVariable v = new LuaDebugVariable(key.toString(), this, debugValue, key.isint(), false);
                xValues.add(v.getName(), v);
            }
            node.addChildren(xValues, true);
        } else {
            super.computeChildren(node);
        }
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        value.computePresentation(node, place);
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable navigatable) {
        final DataManager dataManager = DataManager.getInstance();
        final DataContext dataContext = dataManager != null ? dataManager.getDataContext() : null;
        final Project project;
        if (dataContext != null) {
            project = PlatformDataKeys.PROJECT.getData(dataContext);
        } else {
            super.computeSourcePosition(navigatable);
            return;
        }

        XDebugSession debugSession = null;
        if (project != null) {
            debugSession = XDebuggerManager.getInstance(project).getCurrentSession();
        }
        XSourcePosition currentPosition = null;

        if (debugSession != null) {
            currentPosition = debugSession.getCurrentPosition();
        }

        if (currentPosition == null) return;

        final PsiElement contextElement = XDebuggerUtil.getInstance().findContextElement(currentPosition.getFile(),
                currentPosition.getOffset(), project, false);

        if (contextElement == null) return;

        LuaBlock block = PsiTreeUtil.getParentOfType(contextElement, LuaBlock.class);

        if (!isLocal) {
            block = PsiTreeUtil.getParentOfType(block, LuaBlock.class, true);
        }

        ArrayList<LuaLocalDeclaration> candidates = new ArrayList<>();

        boolean found = false;

        while (block != null && !found) {
            for (LuaLocalDeclaration local : block.getLocals()) {
                final String localName = local.getName();
                if (localName != null && localName.equals(getName())) {
                    candidates.add(local);
                    found = true;
                }
            }

            block = PsiTreeUtil.getParentOfType(block, LuaBlock.class, true);
        }


        if (candidates.size() == 0) return;

        LuaLocalDeclaration resolved = null;
        for (LuaLocalDeclaration candidate : candidates) {
            if (resolved == null) {
                resolved = candidate;
            } else {
                if (candidate.getTextOffset() < contextElement.getTextOffset() && candidate.getTextOffset() > resolved.getTextOffset()) {
                    resolved = candidate;
                }
            }
        }

        navigatable.setSourcePosition(XDebuggerUtil.getInstance().createPositionByElement(resolved));
    }

    @Nullable
    @Override
    public String getEvaluationExpression() {
        if (isIndex) {
            return parent.getName() + "[" + getName() + "]";
        }
        return parent != null ? parent.getName() + "[\"" + getName() + "\"]" : getName();
    }
}
