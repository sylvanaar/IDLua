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

import com.intellij.codeInsight.completion.JavaCompletionUtil;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ArrayUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResult;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResultImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ven
 */
public class LuaParameterInfoHandler implements ParameterInfoHandler<LuaPsiElement, Object> {
  public boolean couldShowInLookup() {
    return true;
  }

  public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
    List<? extends PsiElement> elements = JavaCompletionUtil.getAllPsiElements(item);

    if (elements != null) {
      List<LuaResolveResult> methods = new ArrayList<LuaResolveResult>();
      for (PsiElement element : elements) {
        if (element instanceof PsiMethod) {
          methods.add(new LuaResolveResultImpl(element, true));
        }
      }
      return ArrayUtil.toObjectArray(methods);
    }

    return null;
  }

  public Object[] getParametersForDocumentation(Object resolveResult, ParameterInfoContext context) {
    if (resolveResult instanceof LuaResolveResult) {
      final PsiElement element = ((LuaResolveResult)resolveResult).getElement();
      if (element instanceof PsiMethod) {
        return ((PsiMethod)element).getParameterList().getParameters();
      }
    }

    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public LuaPsiElement findElementForParameterInfo(CreateParameterInfoContext context) {
      try {
    return (LuaPsiElement) context.getFile().findElementAt(context.getOffset()).getContext().getContext();
      } catch (Throwable ignored) {}

      return null;
  }

  public LuaPsiElement findElementForUpdatingParameterInfo(UpdateParameterInfoContext context) {
      try {
    return (LuaPsiElement) context.getFile().findElementAt(context.getOffset()).getContext().getContext();
      } catch (Throwable ignored) {}

      return null;
  }

  public void showParameterInfo(@NotNull LuaPsiElement place, CreateParameterInfoContext context) {
    String text = DocumentationManager.getProviderFromElement(place).getQuickNavigateInfo(place, place);
    if (text == null) return;

    Object[] o = { text };
    context.setItemsToShow(o);
    context.showHint(place, place.getTextRange().getStartOffset(), this);
  }

  public void updateParameterInfo(@NotNull LuaPsiElement place, UpdateParameterInfoContext context) {
  }

  public String getParameterCloseChars() {
    return ",){}";
  }

  public boolean tracksParameterIndex() {
    return false;
  }

  public void updateUI(Object o, ParameterInfoUIContext context) {
    int highlightStartOffset = -1;
    int highlightEndOffset = -1;

    StringBuffer buffer = new StringBuffer();

      
    context.getParameterOwner();

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
