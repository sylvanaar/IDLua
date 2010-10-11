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

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaDeclarationImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 5:01:32 AM
 */
public class LuaFunctionIdentifierImpl extends LuaDeclarationImpl implements LuaFunctionIdentifier  {
    static final Logger log = Logger.getInstance("#Lua.LuaFunctionIdentifierImpl");
    boolean usesSelf = false;
    LuaIdentifier nameNode;
    List<LuaIdentifier> namespaceNodes = new ArrayList<LuaIdentifier>();
    
    
    public LuaFunctionIdentifierImpl(ASTNode node) {
        super(node);

//        if (node.getElementType() == LuaElementTypes.FUNCTION_IDENTIFIER_NEEDSELF)
//            usesSelf = true;

//       // assert node.getLastChildNode() instanceof LuaIdentifier;
//        PsiElement elem = node.getLastChildNode().getPsi();
//        if (elem instanceof LuaIdentifier)
//            nameNode = (LuaIdentifier)elem;
//
//        ASTNode[] names = node.getChildren(LuaElementTypes.IDENTIFIER_SET);
//        for(int i=0; i<names.length-1; i++)
//            namespaceNodes.add((LuaIdentifier) names[i].getPsi());

        log.info("function identifier <"+getNameSpace()+"> <"+getFunctionName()+">");
    }

    @Override
    public boolean isDeclaration() {
        return getContext() instanceof LuaFunctionDefinitionStatement;
    }

//    @Override
//    public boolean getUsesSelf() {
//        return usesSelf;
//    }

    @Override
    public String getFunctionName() {
//        if (nameNode == null ) return "";
//        String s = getNameSpace();
//        if (s.length()>0)
//          s+=usesSelf?":":".";
//
//        return s+nameNode.getText();

        return getText();
    }
    
    @Override
    public String getNameSpace() {
//        String name = "";
//
//        for(LuaIdentifier id : namespaceNodes)
//            name += (id.isField()?".":"")+id.getText();
//
//        return name;

        return "";
    }


}
