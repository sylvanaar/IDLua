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
package com.sylvanaar.idea.Lua.structure;

import com.intellij.psi.*;
import com.sylvanaar.idea.Lua.psi.LuaPsiFile;


/**
 * User: Dmitry.Krasilschikov
 * Date: 21.05.2007
 */
public class LuaElementPresentation {
  public static String getPresentableText(PsiElement element) {
    assert element != null;

    if (element instanceof LuaPsiFile) {
      return getFilePresentableText(((LuaPsiFile) element));

    } else {
      return element.getText();
    }
  }

  public static String getMethodPresentableText(PsiMethod method) {
    StringBuffer presentableText = new StringBuffer();
    presentableText.append(method.getName());
    presentableText.append(" ");

    PsiParameterList paramList = method.getParameterList();
    PsiParameter[] parameters = paramList.getParameters();

    presentableText.append("(");
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) presentableText.append(", ");
      presentableText.append(parameters[i].getType().getPresentableText());
    }
    presentableText.append(")");

    PsiType returnType = method.getReturnType();

    if (returnType != null) {
      presentableText.append(":");
      presentableText.append(returnType.getPresentableText());
    }

    return presentableText.toString();
  }

  public static String getFilePresentableText(LuaPsiFile file) {
    return file.getName();
  }

}