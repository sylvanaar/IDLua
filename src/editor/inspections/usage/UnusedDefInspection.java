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
package com.sylvanaar.idea.Lua.editor.inspections.usage;

import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.*;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.*;
import com.intellij.psi.util.*;
import com.intellij.util.*;
import com.sylvanaar.idea.Lua.editor.inspections.*;
import com.sylvanaar.idea.Lua.lang.psi.*;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.*;
import com.sylvanaar.idea.Lua.lang.psi.dataFlow.*;
import com.sylvanaar.idea.Lua.lang.psi.dataFlow.reachingDefs.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
import gnu.trove.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 & @author ven
 */
public class UnusedDefInspection extends AbstractInspection {
  private static final Logger log = Logger.getInstance("Lua.UnusedDefInspection");


    @Override
    public String getStaticDescription() {
        return "Variable is not used";
    }

    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return DATA_FLOW;
    }

    @Nls
    @NotNull
    public String getDisplayName() {
        return "Unused Assignment";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new LuaElementVisitor() {
//            public void visitFunctionDef(LuaFunctionDefinitionStatement e) {
//                super.visitFunctionDef(e);
//                LuaBlock block = e.getBlock();
//                if (block != null)
//                    check(block, holder);
//            }
//
//            @Override
//            public void visitAnonymousFunction(LuaAnonymousFunctionExpression e) {
//                super.visitAnonymousFunction(e);
//                LuaBlock block = e.getBlock();
//                if (block != null)
//                    check(block, holder);
//            }
            @Override
            public void visitFile(PsiFile file) {
                super.visitFile(file);

                if (file instanceof LuaPsiFile)
                    check((LuaControlFlowOwner) file, holder);
            }
        };
    }    


  protected void check(final LuaControlFlowOwner owner, final ProblemsHolder problemsHolder) {
    final Instruction[] flow = owner.getControlFlow();
    final ReachingDefinitionsDfaInstance dfaInstance = new ReachingDefinitionsDfaInstance(flow);
    final ReachingDefinitionsSemilattice lattice = new ReachingDefinitionsSemilattice();
    final DFAEngine<TIntObjectHashMap<TIntHashSet>> engine = new DFAEngine<TIntObjectHashMap<TIntHashSet>>(flow, dfaInstance, lattice);
    final ArrayList<TIntObjectHashMap<TIntHashSet>> dfaResult = engine.performDFA();
    final TIntHashSet unusedDefs = new TIntHashSet();
    for (Instruction instruction : flow) {
      if (instruction instanceof ReadWriteVariableInstruction && ((ReadWriteVariableInstruction) instruction).isWrite()) {
        unusedDefs.add(instruction.num());
      }
    }

    for (int i = 0; i < dfaResult.size(); i++) {
      final Instruction instruction = flow[i];
      if (instruction instanceof ReadWriteVariableInstruction) {
        final ReadWriteVariableInstruction varInsn = (ReadWriteVariableInstruction) instruction;
        if (!varInsn.isWrite()) {
          final String varName = varInsn.getVariableName();
          TIntObjectHashMap<TIntHashSet> e = dfaResult.get(i);
          e.forEachValue(new TObjectProcedure<TIntHashSet>() {
            public boolean execute(TIntHashSet reaching) {
              reaching.forEach(new TIntProcedure() {
                public boolean execute(int defNum) {
                  final String defName = ((ReadWriteVariableInstruction) flow[defNum]).getVariableName();
                  if (varName.equals(defName)) {
                    unusedDefs.remove(defNum);
                  }
                  return true;
                }
              });
              return true;
            }
          });
        }
      }
    }

    unusedDefs.forEach(new TIntProcedure() {
      public boolean execute(int num) {
        final ReadWriteVariableInstruction instruction = (ReadWriteVariableInstruction)flow[num];
        final PsiElement element = instruction.getElement();
        if (element == null) return true;
        PsiElement toHighlight = null;
        if (isLocalAssignment(element)) {
          if (element instanceof LuaReferenceElement) {
            PsiElement parent = element.getParent();
            if (parent instanceof LuaReferenceElement) {
              toHighlight = ((LuaAssignmentStatement)parent).getLeftExprs();
            }
//            if (parent instanceof GrPostfixExpression) {
//              toHighlight = parent;
//            }
          }
          else if (element instanceof LuaSymbol) {
            toHighlight = ((LuaSymbol)element);//.getNamedElement();
          }
          if (toHighlight == null) toHighlight = element;
          problemsHolder.registerProblem(toHighlight, "Unused Assignment",
                                         ProblemHighlightType.LIKE_UNUSED_SYMBOL);
        }
        return true;
      }
    });
  }

  private boolean isUsedInToplevelFlowOnly(PsiElement element) {
    LuaSymbol var = null;
    if (element instanceof LuaSymbol) {
      var = (LuaSymbol) element;
    } else if (element instanceof LuaReferenceElement) {
      final PsiElement resolved = ((LuaReferenceElement) element).resolve();
      if (resolved instanceof LuaSymbol) var = (LuaSymbol) resolved;
    }

    if (var != null) {
      final LuaPsiElement scope = getScope(var);
      if (scope == null) {
        PsiFile file = var.getContainingFile();
        log.error(file == null ? "no file???" : DebugUtil.psiToString(file, true, false));
      }

      return ReferencesSearch.search(var, new LocalSearchScope(scope)).forEach(new Processor<PsiReference>() {
        public boolean process(PsiReference ref) {
          return getScope(ref.getElement()) == scope;
        }
      });
    }

    return true;
  }

  private LuaPsiElement getScope(PsiElement var) {
    return PsiTreeUtil.getParentOfType(var, LuaBlock.class, LuaPsiFile.class);
  }

  private boolean isLocalAssignment(PsiElement element) {
    if (element instanceof LuaSymbol) {
      return isLocalVariable((LuaSymbol) element, false);
    } else if (element instanceof LuaReferenceElement) {
      final PsiElement resolved = ((LuaReferenceElement) element).resolve();
      return resolved instanceof LuaSymbol && isLocalVariable((LuaSymbol) resolved, true);
    }

    return false;
  }

  private boolean isLocalVariable(LuaSymbol var, boolean parametersAllowed) {
    if (var instanceof LuaLocal) return true;
    else if (var instanceof LuaParameter && !parametersAllowed) return false;

    return false;
  }

  public boolean isEnabledByDefault() {
    return true;
  }
}
