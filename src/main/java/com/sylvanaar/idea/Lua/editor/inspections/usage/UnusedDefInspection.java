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

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ex.UnfairLocalInspectionTool;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.sylvanaar.idea.Lua.editor.inspections.AbstractInspection;
import com.sylvanaar.idea.Lua.lang.psi.LuaControlFlowOwner;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.Instruction;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.ReadWriteVariableInstruction;
import com.sylvanaar.idea.Lua.lang.psi.dataFlow.DFAEngine;
import com.sylvanaar.idea.Lua.lang.psi.dataFlow.reachingDefs.ReachingDefinitionsDfaInstance;
import com.sylvanaar.idea.Lua.lang.psi.dataFlow.reachingDefs.ReachingDefinitionsSemilattice;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocal;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 & @author ven
 */
public class UnusedDefInspection extends AbstractInspection implements UnfairLocalInspectionTool {
  private static final Logger log = Logger.getInstance("Lua.UnusedDefInspection");


    @Override
    public String getStaticDescription() {
        return "Variable is not used";
    }

    @Override
    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return DATA_FLOW;
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Override
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

                if (file instanceof LuaPsiFile) try {
                    check((LuaControlFlowOwner) file, holder);
                } catch (Exception ignored) {
                    log.debug(ignored);
                }
            }
        };
    }    


  protected static void check(final LuaControlFlowOwner owner, final ProblemsHolder problemsHolder) {
    final Instruction[] flow = owner.getControlFlow();
    if (flow == null) return;
    final ReachingDefinitionsDfaInstance dfaInstance = new ReachingDefinitionsDfaInstance(flow);
    final ReachingDefinitionsSemilattice lattice = new ReachingDefinitionsSemilattice();
    final DFAEngine<TIntObjectHashMap<TIntHashSet>> engine = new DFAEngine<TIntObjectHashMap<TIntHashSet>>(flow, dfaInstance, lattice);
    final ArrayList<TIntObjectHashMap<TIntHashSet>> dfaResult = engine.performDFA();
    final TIntHashSet unusedDefs = new TIntHashSet();
    for (Instruction instruction : flow) {
      if (instruction instanceof ReadWriteVariableInstruction && ((ReadWriteVariableInstruction) instruction).isWrite()) {
          if (!((ReadWriteVariableInstruction) instruction).getVariableName().equals("_"))
            unusedDefs.add(instruction.num());
      }
    }

    ProgressIndicatorProvider.checkCanceled();

    for (int i = 0; i < dfaResult.size(); i++) {
      final Instruction instruction = flow[i];
      if (instruction instanceof ReadWriteVariableInstruction) {
        final ReadWriteVariableInstruction varInsn = (ReadWriteVariableInstruction) instruction;
        if (!varInsn.isWrite()) {
          final LuaSymbol var = varInsn.getSymbol();
          TIntObjectHashMap<TIntHashSet> e = dfaResult.get(i);
          e.forEachValue(new TObjectProcedure<TIntHashSet>() {
            @Override
            public boolean execute(TIntHashSet reaching) {
              reaching.forEach(new TIntProcedure() {
                @Override
                public boolean execute(int defNum) {
                  LuaSymbol defName = ((ReadWriteVariableInstruction) flow[defNum]).getSymbol();
                  if (defName.getReference() != null) {
                      defName = (LuaSymbol) defName.getReference().resolve();
                  }
                  if (var != null && var.equals(defName)) {
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

    ProgressIndicatorProvider.checkCanceled();

      unusedDefs.forEach(new TIntProcedure() {
          @Override
          public boolean execute(int num) {
              final ReadWriteVariableInstruction instruction = (ReadWriteVariableInstruction) flow[num];
              final PsiElement element = instruction.getElement();
              if (element == null) return true;
              if (isLocalAssignment(element)) {
                  PsiElement toHighlight = null;
                  if (element instanceof LuaReferenceElement) {
                      PsiElement parent = element.getParent();
                      if (parent instanceof LuaIdentifierList)
                          parent = parent.getParent();

                      if (parent instanceof LuaAssignmentStatement) {
                          toHighlight = element;
                      }
                  } else if (element instanceof LuaSymbol) {
                      toHighlight = ((LuaSymbol) element);//.getNamedElement();
                  }
                  if (toHighlight == null) toHighlight = element;

                  if (toHighlight.getTextLength() > 0)
                  problemsHolder
                          .registerProblem(toHighlight, "Unused Assignment", ProblemHighlightType.LIKE_UNUSED_SYMBOL);
              }
              return true;
          }
      });
  }

//  private boolean isUsedInToplevelFlowOnly(PsiElement element) {
//    LuaSymbol var = null;
//    if (element instanceof LuaSymbol) {
//      var = (LuaSymbol) element;
//    } else if (element instanceof LuaReferenceElement) {
//      final PsiElement resolved = ((LuaReferenceElement) element).resolve();
//      if (resolved instanceof LuaSymbol) var = (LuaSymbol) resolved;
//    }
//
//    if (var != null) {
//      final LuaPsiElement scope = getScope(var);
//      if (scope == null) {
//        PsiFile file = var.getContainingFile();
//        log.error(file == null ? "no file???" : DebugUtil.psiToString(file, true, false));
//      }
//
//      return ReferencesSearch.search(var, new LocalSearchScope(scope)).forEach(new Processor<PsiReference>() {
//        public boolean process(PsiReference ref) {
//          return getScope(ref.getElement()) == scope;
//        }
//      });
//    }
//
//    return true;
//  }

//  private LuaPsiElement getScope(PsiElement var) {
//    return PsiTreeUtil.getParentOfType(var, LuaBlock.class, LuaPsiFile.class);
//  }

  private static boolean isLocalAssignment(PsiElement element) {
    if (element instanceof LuaSymbol) {
      return isLocalVariable((LuaSymbol) element, false);
    } else if (element instanceof LuaReferenceElement) {
      final PsiElement resolved = ((PsiReference) element).resolve();
      return resolved instanceof LuaSymbol && isLocalVariable((LuaSymbol) resolved, true);
    }

    return false;
  }

  private static boolean isLocalVariable(LuaSymbol var, boolean parametersAllowed) {
    if (var instanceof LuaLocal) return true;
    else if (var instanceof LuaParameter && !parametersAllowed) return false;

    return false;
  }
}
