package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.codeInsight.TargetElementEvaluator;
import com.intellij.codeInsight.TargetElementEvaluatorEx2;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalIdentifier;
import org.jetbrains.annotations.NotNull;

public class LuaTargetElementEvaluator extends TargetElementEvaluatorEx2 implements TargetElementEvaluator {
    @Override
    public boolean isAcceptableNamedParent(@NotNull PsiElement parent) {
        if (parent instanceof LuaLocalIdentifier) {
            return ((LuaLocalIdentifier) parent).isAssignedTo();
        }
        return super.isAcceptableNamedParent(parent);
    }
}
