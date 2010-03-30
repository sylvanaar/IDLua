/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.sylvanaar.idea.Lua.psi.util;

import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.psi.LuaElement;
import com.sylvanaar.idea.Lua.psi.statements.LuaStatement;
import com.sylvanaar.idea.Lua.psi.statements.LuaVariable;
import com.sylvanaar.idea.Lua.psi.statements.LuaVariableDeclaration;


/**
 * @author ilyas
 */
public interface LuaVariableDeclarationOwner extends LuaElement {

  /**
   * Removes variable from its declaration. In case of alone variablein declaration,
   * it also will be removed.
   * @param variable to remove
   * @throws com.intellij.util.IncorrectOperationException in case the operation cannot be performed
   */
  void removeVariable(LuaVariable variable);

  /**
   * Adds new variable declaration after anchor spectified. If anchor == null, adds variable at owner's first position
   * @param declaration declaration to insert 
   * @param anchor Anchor after which new variabler declaration will be placed
   * @return inserted variable declaration
   * @throws com.intellij.util.IncorrectOperationException in case the operation cannot be performed
   */
 LuaVariableDeclaration addVariableDeclarationBefore(LuaVariableDeclaration declaration, LuaStatement anchor) throws IncorrectOperationException;

}
