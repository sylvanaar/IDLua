package com.sylvanaar.idea.Lua.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.psi.impl.statements.LuaStatementElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBreakStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaConditionalLoop;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;

/**
 * Created by Jon on 10/23/2016.
 */
public class LuaBreakStatementImpl extends LuaStatementElementImpl implements LuaBreakStatement {
    public LuaBreakStatementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public LuaStatementElement findTargetStatement() {
        return PsiTreeUtil.getParentOfType(this, LuaConditionalLoop.class);
    }
}
