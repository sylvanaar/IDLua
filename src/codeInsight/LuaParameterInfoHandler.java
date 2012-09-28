/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sylvanaar.idea.Lua.codeInsight;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFunctionCallExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author ven
 */
public class LuaParameterInfoHandler implements ParameterInfoHandler<LuaPsiElement, Object> {
    public boolean couldShowInLookup() {
        return true;
    }

    public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return null;
    }

    public Object[] getParametersForDocumentation(Object resolveResult, ParameterInfoContext context) {
        return null;
    }

    public LuaPsiElement findElementForParameterInfo(CreateParameterInfoContext context) {
        return findCall(context.getFile(), context.getOffset());
    }

    public LuaPsiElement findElementForUpdatingParameterInfo(UpdateParameterInfoContext context) {
        return findCall(context.getFile(), context.getOffset());
    }

    public static LuaPsiElement findCall(PsiFile file, int offset) {
        PsiElement func;
        int delta = 0;

        try {
            do {
                func = file.findElementAt(offset - delta++);
            } while (func instanceof PsiWhiteSpace);

            // System.out.println("Element at pos:" + func);

            do {
                if (func instanceof LuaFunctionCallExpression)
                    return (LuaPsiElement) func;

                func = func.getContext();
            } while (func != null);

        } catch (Throwable ignored) {
        }

        return null;
    }


    public void showParameterInfo(@NotNull LuaPsiElement place, CreateParameterInfoContext context) {
        if (!(place instanceof LuaFunctionCallExpression))
            return;

        final LuaReferenceElement functionNameElement = ((LuaFunctionCallExpression) place).getFunctionNameElement();
        if (functionNameElement == null) return;


        String text = DocumentationManager.getProviderFromElement(place).
                getQuickNavigateInfo(functionNameElement.resolve(), place);
        
        if (text == null) return;

        Object[] o = {text};
        context.setItemsToShow(o);
        context.showHint(place, place.getTextRange().getStartOffset(), this);
    }

    public void updateParameterInfo(@NotNull LuaPsiElement place, UpdateParameterInfoContext context) {
    }

    public String getParameterCloseChars() {
        return ",){}\t";
    }

    public boolean tracksParameterIndex() {
        return false;
    }

    public void updateUI(Object o, ParameterInfoUIContext context) {
        int highlightStartOffset = -1;
        int highlightEndOffset = -1;

        StringBuilder buffer = new StringBuilder();


        PsiElement owningElement = context.getParameterOwner();

        if (o instanceof LuaPsiElement)
            buffer.append(((LuaPsiElement) o).getText());

        if (o instanceof String)
            buffer.append(o);

        context.setupUIComponentPresentation(
                buffer.toString(),
                highlightStartOffset,
                highlightEndOffset,
                !context.isUIComponentEnabled(),
                false,
                false,
                context.getDefaultParameterColor()
        );
    }


}
