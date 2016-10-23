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
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/28/11
 * Time: 11:07 AM
 */
public class LuaStackFrame extends XStackFrame {
    private XSourcePosition mySourcePosition = null;
    private Project myProject;
    private LuaDebuggerController myController = null;
    private final String contextName;
    private int myIndex;

    LuaRemoteStack remoteStack = null;

    LuaStackFrame(Project project, LuaDebuggerController controller, XSourcePosition position, int index) {
        this(project, controller, position, null, index);
    }

    LuaStackFrame(Project project, LuaDebuggerController controller, XSourcePosition position, String contextName, int index) {
        mySourcePosition = position;
        myProject = project;
        myController = controller;
        this.contextName = contextName;
        myIndex = index;
    }

    @Override
    public XSourcePosition getSourcePosition() {
        return mySourcePosition;
    }

    @Override
    public XDebuggerEvaluator getEvaluator() {
        return new LuaDebuggerEvaluator(myController);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        Promise<LuaRemoteStack> stack = myController.variables();
        if (stack == null) return;
        final XCompositeNode compositeNode = node;
        stack.done(stack1 -> {
            final XValueChildrenList xValues = new XValueChildrenList();

            final List<LuaDebugVariable> locals = stack1.getLocals(myIndex);
            if (locals.size() > 0) {
                xValues.addTopGroup(new XValueGroup("Locals") {
                    @Override
                    public boolean isAutoExpand() {
                        return true;
                    }

                    @Override
                    public void computeChildren(@NotNull XCompositeNode node) {
                        final XValueChildrenList xValues = new XValueChildrenList();

                        for (LuaDebugVariable v : locals) xValues.add(v.getName(), v);
                        node.addChildren(xValues, true);
                        node.setAlreadySorted(false);
                    }
                });
            }

            final List<LuaDebugVariable> upvalues = stack1.getUpvalues(myIndex);
            if (upvalues.size() > 0) {
                xValues.addTopGroup(new XValueGroup("Upvalues") {
                    @Override
                    public boolean isAutoExpand() {
                        return true;
                    }

                    @Override
                    public void computeChildren(@NotNull XCompositeNode node) {
                        final XValueChildrenList xValues = new XValueChildrenList();

                        for (LuaDebugVariable v : upvalues) xValues.add(v.getName(), v);
                        node.addChildren(xValues, true);
                        node.setAlreadySorted(false);
                    }
                });
            }

            compositeNode.addChildren(xValues, true);
            compositeNode.setAlreadySorted(false);
        });
    }

    public void customizePresentation(@NotNull ColoredTextContainer component) {
        if (mySourcePosition != null) {
            super.customizePresentation(component);

            if (contextName != null){
                component.append(String.format(" (%s)", contextName), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
            }
        } else {
            component.append("<internal C>", SimpleTextAttributes.GRAYED_ATTRIBUTES);
            component.setIcon(AllIcons.Debugger.StackFrame);
        }
    }
}
