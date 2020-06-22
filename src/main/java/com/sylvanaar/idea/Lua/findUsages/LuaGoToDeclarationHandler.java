package com.sylvanaar.idea.Lua.findUsages;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaLocalIdentifierImpl;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalIdentifier;
import org.jetbrains.annotations.Nullable;

public class LuaGoToDeclarationHandler implements GotoDeclarationHandler {
    private static final PsiElement[] EMPTY_ARRAY = new PsiElement[0];
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement instanceof LeafPsiElement) {
            sourceElement = sourceElement.getParent();
        }
        if (sourceElement instanceof LuaLocalIdentifierImpl) {
            return new PsiElement[]{((LuaLocalIdentifierImpl) sourceElement).getResolvedElement()};
        }

        return EMPTY_ARRAY;
    }
}
