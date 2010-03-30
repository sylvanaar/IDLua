/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.sylvanaar.idea.Lua.psi.statements.expressions;

import org.jetbrains.annotations.Nullable;

/**
 * @author ilyas
 */
public interface LuaSelfReferenceExpression extends LuaExpression {
  @Nullable
 LuaReferenceExpression getQualifier();
}
