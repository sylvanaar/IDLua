package com.sylvanaar.idea.Lua.lang.psi.util;

import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalDeclaration;

import java.util.List;

/**
 * Created by Jon on 10/23/2016.
 */
public interface LuaBlockVariablesProvider extends PsiElement {
    List<? extends LuaLocalDeclaration> getProvidedVariables();
}
