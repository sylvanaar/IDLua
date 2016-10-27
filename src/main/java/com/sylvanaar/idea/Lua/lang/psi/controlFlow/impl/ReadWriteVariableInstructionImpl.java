/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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
package com.sylvanaar.idea.Lua.lang.psi.controlFlow.impl;

import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.ReadWriteVariableInstruction;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;


/**
 * @author ven
*/
class ReadWriteVariableInstructionImpl extends InstructionImpl implements ReadWriteVariableInstruction {
  private final boolean myIsWrite;
  public String myName;
  LuaSymbol mySymbol;

//  ReadWriteVariableInstructionImpl(String varName, LuaSymbol element, int num, boolean isWrite) {
//    super(element, num);
//
//    myName = varName;
//    myIsWrite = isWrite;
//  }

  ReadWriteVariableInstructionImpl(LuaSymbol variable, int num, boolean isWrite) {
    super(variable, num);
    myName = variable.getName();
    mySymbol = variable;
    myIsWrite = isWrite;
  }

  ReadWriteVariableInstructionImpl(LuaReferenceElement refExpr, int num, boolean isWrite) {
    super(refExpr, num);
    myName = refExpr.getName();
    myIsWrite = isWrite;
    mySymbol = (LuaSymbol) refExpr.resolve();
  }

  public String getVariableName() {

    return myName;
  }

  public boolean isWrite() {
    return myIsWrite;
  }

    @Override
    public boolean isGlobal() {
        return mySymbol instanceof LuaGlobal;
    }

    @Override
    public boolean isField() {
        return mySymbol instanceof LuaCompoundIdentifier;
    }

    @Override
    public LuaSymbol getSymbol() {
        return mySymbol;
    }

    protected String getElementPresentation() {
        String kind = "GLOBAL";
        
        if (isField())
            kind = "FIELD";
        else if (!isGlobal()) {
            if (mySymbol instanceof LuaParameter)
                kind = "PARAMETER";
            else if (mySymbol instanceof LuaUpvalueIdentifier)
                kind = "UPVALUE";
            else
                kind = "LOCAL";
        }


    return String.format("%s %s %s", isWrite() ? "WRITE" : "READ", kind, getVariableName());
  }
}
