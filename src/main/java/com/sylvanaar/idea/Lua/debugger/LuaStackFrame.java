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

package com.sylvanaar.idea.Lua.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Consumer;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

import javax.swing.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/28/11
 * Time: 11:07 AM
 */
public class LuaStackFrame extends XStackFrame {
    XSourcePosition mySourcePosition = null;
    private Project myProject;
    LuaDebuggerController myController = null;
    int myIndex;

    LuaStackFrame(Project project, LuaDebuggerController controller, XSourcePosition position, int index) {
        mySourcePosition = position;
        myProject = project;
        myController = controller;
        myIndex = index;
    }

    @Override
    public XSourcePosition getSourcePosition() {
        return mySourcePosition;
    }

    @Override
    public XDebuggerEvaluator getEvaluator() {
        return new LuaDebuggerEvaluator(myProject, this, myController);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        Promise<List<LuaDebugVariable>> variables = myController.variables(myIndex);
        if (variables == null) return;
        final XCompositeNode compositeNode = node;
        variables.done(new Consumer<List<LuaDebugVariable>>() {
            @Override
            public void consume(List<LuaDebugVariable> variables) {
                final XValueChildrenList xValues = new XValueChildrenList(variables.size());
                for (LuaDebugVariable v : variables) xValues.add(v.getName(), v);
                compositeNode.addChildren(xValues, true);
                compositeNode.setAlreadySorted(false);
            }
        });
    }

    public void customizePresentation(@NotNull ColoredTextContainer component) {
        if (mySourcePosition != null) {
            super.customizePresentation(component);
        } else {
            component.append("<internal C>", SimpleTextAttributes.GRAYED_ATTRIBUTES);
            component.setIcon(AllIcons.Debugger.StackFrame);
        }
    }
}
