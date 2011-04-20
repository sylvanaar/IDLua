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

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.sylvanaar.idea.Lua.editor.inspections.AbstractInspection;
import com.sylvanaar.idea.Lua.lang.psi.LuaControlFlowOwner;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.Instruction;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.ReadWriteVariableInstruction;
import com.sylvanaar.idea.Lua.lang.psi.dataFlow.DFAEngine;
import com.sylvanaar.idea.Lua.lang.psi.dataFlow.reachingDefs.ReachingDefinitionsDfaInstance;
import com.sylvanaar.idea.Lua.lang.psi.dataFlow.reachingDefs.ReachingDefinitionsSemilattice;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocal;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


import java.util.ArrayList;

/**
 & @author ven
 */
public class UnusedDefInspection extends AbstractInspection {
  private static final Logger log = Logger.getInstance("#Lua.UnusedDefInspection");


    @Override
    public String getStaticDescription() {
        return "Variable is not used";
    }

    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return "Data Flow Issues";
    }

    @Nls
    @NotNull
    public String getDisplayName() {
        return "Unused Assignment";
    }

    @NonNls
    @NotNull
    public String getShortName() {
        return "LuaUnusedAssignment";
    }


    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new LuaElementVisitor() {
//            @Override
//            public void visitBlock(LuaBlock e) {
//                super.visitBlock(e);
//
//                check(e, holder);
//            }

            @Override
            public void visitFile(PsiFile file) {
                super.visitFile(file);

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
    return false;
  }
}
