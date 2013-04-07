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

package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.codeInsight.highlighting.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/17/11
 * Time: 1:37 AM
 */
public class LuaReadWriteAccessDetector extends ReadWriteAccessDetector {
    @Override
    public boolean isReadWriteAccessible(PsiElement element) {
        return element instanceof LuaSymbol;
    }

    @Override
    public boolean isDeclarationWriteAccess(PsiElement element) {
        if (! (element instanceof LuaSymbol))
            return false;

        if (element instanceof LuaFieldIdentifier) {
            final LuaSymbol enclosing = ((LuaFieldIdentifier) element).getCompositeIdentifier();
            if (enclosing != null && enclosing.equals(element.getParent()))
                return enclosing.isAssignedTo();
            else if (element.getParent() instanceof LuaKeyValueInitializer)
                return true;
        }

        if (element instanceof LuaParameter) {
            return true;
        }

        if (element instanceof LuaModuleExpression)
            return true;

        LuaStatementElement stmt = PsiTreeUtil.getParentOfType(element, LuaStatementElement.class);
        if (stmt == null) return false;
        
        if (stmt instanceof LuaGenericForStatement)
            return true;

        if (stmt instanceof LuaNumericForStatement)
            return true;

        if (stmt instanceof LuaFunctionDefinitionStatement)
            return ((LuaFunctionDefinitionStatement) stmt).getIdentifier().equals(element);

        if (stmt instanceof LuaAssignmentStatement) {
            if (((LuaAssignmentStatement) stmt).getRightExprs() == null)
                return false;

            for(LuaAssignment a : ((LuaAssignmentStatement) stmt).getAssignments())
                if (a.getSymbol() == element) return true;
        }
                
        return false;
    }

  public Access getReferenceAccess(final PsiElement referencedElement, final PsiReference reference) {
      final PsiElement element = reference.getElement();
      if (element.getParent().getParent() instanceof LuaFunctionDefinitionStatement)
          return Access.Write;
      
      if (element instanceof LuaCompoundIdentifier) {
          if (((LuaCompoundIdentifier) element).isCompoundDeclaration()) return Access.Write;
      } else {
          if (element instanceof LuaFieldIdentifier)
              return ((LuaFieldIdentifier) element).isAssignedTo() ? Access.Write : Access.Read;
          if (element instanceof LuaDeclarationExpression) return Access.Write;
      }
      
      return LuaPsiUtils.isLValue((LuaPsiElement) reference) ? Access.Write : Access.Read;
  }

  public Access getExpressionAccess(final PsiElement expression) {


    return LuaPsiUtils.isLValue((LuaPsiElement) expression) ? Access.Write : Access.Read;
  }
}
