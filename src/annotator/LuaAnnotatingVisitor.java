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

package com.sylvanaar.idea.Lua.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.util.Range;
import com.sylvanaar.idea.Lua.LuaBundle;
import com.sylvanaar.idea.Lua.LuaKeywordsManager;
import com.sylvanaar.idea.Lua.configurator.LuaServersConfiguration;
import com.sylvanaar.idea.Lua.configurator.LuaServerDescriptor;
import com.sylvanaar.idea.Lua.psi.*;



public class LuaAnnotatingVisitor extends LuaElementVisitor implements Annotator {

    private AnnotationHolder holder;

    private LuaKeywordsManager keywords;
    private LuaServersConfiguration configuration;

    public LuaAnnotatingVisitor(LuaKeywordsManager keywords, LuaServersConfiguration configuration) {
        this.keywords = keywords;
        this.configuration = configuration;
    }

    public synchronized void annotate(PsiElement psiElement, AnnotationHolder holder) {
        this.holder = holder;
        psiElement.accept(this);
        this.holder = null;
    }

    @Override
    public void visitDirective(LuaDirective node) {

        if (node.isInChaosContext()) {
            return; //directive resides in context like charset_map where almost arbitrary contents are possible
        }
        if (!checkNameIsLegal(node.getDirectiveName())) {
            return; //name is not known - no point for further investigation
        }

        //ok, now we know that directive does exist. let's do some more advanced checks.
        checkParentContext(node);
        checkChildContext(node);
        checkValueCount(node);

    }

    @Override
    public void visitComplexValue(LuaComplexValue node) {

        if (node.getDirective().isInChaosContext()) return;

        String directiveName = node.getDirective().getNameString();
        if (keywords.checkBooleanKeyword(directiveName)) {
            checkBooleanValue(node, directiveName);
        }
    }

    @Override
    public void visitInnerVariable(LuaInnerVariable node) {

        //should I cut $ in LuaInnerVariable itself?
        if (!keywords.isValidInnerVariable(node.getName())) {
            holder.createWarningAnnotation(node, LuaBundle.message("annotator.variable.notexists", node.getText()));
        }

    }

    private void checkValueCount(LuaDirective node) {

        int realRange = node.getValues().size();
        Range<Integer> expectedRange = keywords.getValueRange(node.getNameString());

        if (!expectedRange.isWithin(realRange)) {

            String rangeString;
            if (expectedRange.getFrom().equals(expectedRange.getTo())) {
                rangeString = expectedRange.getFrom().toString();
            } else {
                rangeString = "[" + expectedRange.getFrom() + ", " + expectedRange.getTo() + "]";
            }
            String message = LuaBundle.message("annotator.directive.wrongnumberofvalues", node.getNameString(), rangeString, realRange);

            int i = 0;
            for (LuaComplexValue LuaComplexValue : node.getValues()) {
                if (++i > expectedRange.getTo()) {
                    holder.createErrorAnnotation(LuaComplexValue, message);
                }
            }
            if (i == 0) {
                holder.createErrorAnnotation(node.getDirectiveName(), message);
            }

        }

    }

    private void checkChildContext(LuaDirective node) {
        if (node.hasContext() && !keywords.checkCanHaveChildContext(node.getNameString())) {
            holder.createErrorAnnotation(node, LuaBundle.message("annotator.directive.canthavecontext", node.getNameString()));
        }
    }

    private void checkParentContext(LuaDirective node) {
        LuaContext parentContext = node.getParentContext();
        if (parentContext == null) {
            //top level directive checks are made only main file. other files can be potentially included 
            if (nodeInMainConfig(node) && !keywords.checkCanResideInMainContext(node.getNameString())) {
                holder.createWarningAnnotation(node, LuaBundle.message("annotator.directive.cantbeinmain", node.getNameString()));
            }
        } else {
            LuaDirective parent = parentContext.getDirective();
            if (!keywords.checkCanHaveParentContext(node.getNameString(), parent.getNameString())) {
                holder.createWarningAnnotation(node, node.getNameString() + " cant reside in " + parent.getNameString());
            }

        }
    }

    private boolean nodeInMainConfig(LuaDirective node) {
        boolean isInMainConfig = false;
        LuaServerDescriptor[] serversDescriptors = configuration.getServersDescriptors();
        for (LuaServerDescriptor serversDescriptor : serversDescriptors) {
            if (serversDescriptor.getConfigPath().equals(node.getContainingFile().getVirtualFile().getPath())) {
                isInMainConfig = true;
                break;
            }
        }
        return isInMainConfig;
    }

    private void checkBooleanValue(LuaComplexValue node, String directiveName) {
        if (node.isFirstValue()) {
            if (!("on".equals(node.getText()) || "off".equals(node.getText()))) {
                holder.createErrorAnnotation(node, LuaBundle.message("annotator.expected.boolean"));
            }
        } else {
            holder.createErrorAnnotation(node, LuaBundle.message("annotator.not.boolean", directiveName));
        }
    }

    private boolean checkNameIsLegal(LuaDirectiveName node) {

        if (keywords.getKeywords().contains(node.getText())) {
            return true;
        } else {
            holder.createWarningAnnotation(node, LuaBundle.message("annotator.directive.unknown", node.getText()));
            return false;
        }

    }

}



