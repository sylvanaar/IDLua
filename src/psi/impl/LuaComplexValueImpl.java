package com.sylvanaar.idea.Lua.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.annotator.LuaElementVisitor;
import com.sylvanaar.idea.Lua.psi.LuaComplexValue;
import com.sylvanaar.idea.Lua.psi.LuaDirective;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 15.08.2009
 * Time: 20:01:27
 */
public class LuaComplexValueImpl extends LuaElementImpl implements LuaComplexValue {

    public LuaComplexValueImpl(@NotNull ASTNode node) {
        super(node);
    }


    public LuaDirective getDirective() {
        return (LuaDirective) getNode().getTreeParent().getPsi();
    }

    public boolean isFirstValue() {
        return !(getNode().getTreePrev().getTreePrev().getPsi() instanceof LuaComplexValue);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitComplexValue(this);
        } else {
            visitor.visitElement(this);
        }
    }
}
