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
package com.sylvanaar.idea.Lua.lang.psi.dataFlow.reachingDefs;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaControlFlowOwner;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFileBase;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.ControlFlowUtil;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.Instruction;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.ReadWriteVariableInstruction;
import com.sylvanaar.idea.Lua.lang.psi.dataFlow.DFAEngine;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectProcedure;
import gnu.trove.TIntProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author ven
 */
public class ReachingDefinitionsCollector {
  private ReachingDefinitionsCollector() {
  }

  public static FragmentVariableInfos obtainVariableFlowInformation(final LuaStatementElement first, final LuaStatementElement last) {
    LuaPsiElement context = PsiTreeUtil.getParentOfType(first, LuaBlock.class, LuaPsiFileBase.class);
    LuaControlFlowOwner flowOwner;

    flowOwner = (LuaControlFlowOwner) context;

    assert flowOwner != null;
    assert PsiTreeUtil.isAncestor(flowOwner, last, true);

    final Instruction[] flow = flowOwner.getControlFlow();
    final ReachingDefinitionsDfaInstance dfaInstance = new ReachingDefinitionsDfaInstance(flow);
    final ReachingDefinitionsSemilattice lattice = new ReachingDefinitionsSemilattice();
    final DFAEngine<TIntObjectHashMap<TIntHashSet>> engine = new DFAEngine<TIntObjectHashMap<TIntHashSet>>(flow, dfaInstance, lattice);
    final TIntObjectHashMap<TIntHashSet> dfaResult = postprocess(engine.performDFA(), flow, dfaInstance);

    final LinkedHashSet<Integer> fragmentInstructions = getFragmentInstructions(first, last, flow);
    final int[] postorder = ControlFlowUtil.postorder(flow);
    LinkedHashSet<Integer> reachableFromFragmentReads = getReachable(fragmentInstructions, flow, dfaResult, postorder);
    LinkedHashSet<Integer> fragmentReads = filterReads(fragmentInstructions, flow);

    final Map<String, VariableInfo> imap = new LinkedHashMap<String, VariableInfo>();
    final Map<String, VariableInfo> omap = new LinkedHashMap<String, VariableInfo>();

    final PsiManager manager = first.getManager();

    for (final Integer ref : fragmentReads) {
      ReadWriteVariableInstruction rwInstruction = (ReadWriteVariableInstruction) flow[ref];
      String name = rwInstruction.getVariableName();
      final int[] defs = dfaResult.get(ref).toArray();
      if (!allDefsInFragment(defs, fragmentInstructions)) {
        addVariable(name, imap, manager, getType(rwInstruction.getElement()));
      }
    }

    for (final Integer ref : reachableFromFragmentReads) {
      ReadWriteVariableInstruction rwInstruction = (ReadWriteVariableInstruction) flow[ref];
        String name = rwInstruction.getVariableName();
        final int[] defs = dfaResult.get(ref).toArray();
        if (anyDefInFragment(defs, fragmentInstructions)) {
          for (int def : defs) {
            if (fragmentInstructions.contains(def)) {
              LuaType outputType = getType(flow[def].getElement());
              addVariable(name, omap, manager, outputType);
            }
          }

          if (!allProperDefsInFragment(defs, ref, fragmentInstructions, postorder)) {
            LuaType inputType = getType(rwInstruction.getElement());
            addVariable(name, imap, manager, inputType);
          }
        }
    }

  //  addClosureUsages(imap, omap, first, last, flowOwner);

    final VariableInfo[] iarr = filterNonlocals(imap, first);
    final VariableInfo[] oarr = filterNonlocals(omap, first);

    return new FragmentVariableInfos() {
      public VariableInfo[] getInputVariableNames() {
        return iarr;
      }

      public VariableInfo[] getOutputVariableNames() {
        return oarr;
      }
    };
  }

//  private static void addClosureUsages(final Map<String, VariableInfo> imap, final Map<String, VariableInfo> omap, final LuaStatement first, final LuaStatement last, LuaControlFlowOwner flowOwner) {
//    flowOwner.accept(new LuaRecursiveElementVisitor() {
//      public void visitClosure(LuaClosableBlock closure) {
//        addUsagesInClosure(imap, omap, closure, first, last);
//        super.visitClosure(closure);
//      }
//
//      private void addUsagesInClosure(final Map<String, VariableInfo> imap, final Map<String, VariableInfo> omap, final LuaClosableBlock closure, final LuaStatement first, final LuaStatement last) {
//        closure.accept(new LuaRecursiveElementVisitor() {
//          public void visitReferenceExpression(LuaReferenceExpression refExpr) {
//            if (refExpr.isQualified()) {
//              return;
//            }
//            PsiElement resolved = refExpr.resolve();
//            if (!(resolved instanceof LuaVariable)) {
//              return;
//            }
//            LuaVariable variable = (LuaVariable) resolved;
//            if (PsiTreeUtil.isAncestor(closure, variable, true)) {
//              return;
//            }
//            if (variable instanceof ClosureSyntheticParameter &&
//                PsiTreeUtil.isAncestor(closure, ((ClosureSyntheticParameter)variable).getClosure(), false)) {
//              return;
//            }
//
//            String name = variable.getName();
//            if (name != null) {
//              if (!(variable instanceof LuaField)) {
//                if (!isInFragment(first, last, resolved)) {
//                  if (isInFragment(first, last, closure)) {
//                    addVariable(name, imap, variable.getManager(), variable.getType());
//                  }
//                } else {
//                  if (!isInFragment(first, last, closure)) {
//                    addVariable(name, omap, variable.getManager(), variable.getType());
//                  }
//                }
//              }
//            }
//          }
//        });
//      }
//    });
//  }

  private static void addVariable(String name, Map<String, VariableInfo> map, PsiManager manager, LuaType type) {
    VariableInfoImpl info = (VariableInfoImpl) map.get(name);
    if (info == null) {
      info = new VariableInfoImpl(name, manager);
      map.put(name, info);
    }
    info.addSubtype(type);
  }

  private static LinkedHashSet<Integer> filterReads(final LinkedHashSet<Integer> instructions, final Instruction[] flow) {
    final LinkedHashSet<Integer> result = new LinkedHashSet<Integer>();
    for (final Integer i : instructions) {
      final Instruction instruction = flow[i];
      if (instruction instanceof ReadWriteVariableInstruction && !((ReadWriteVariableInstruction) instruction).isWrite()) {
        result.add(i);
      }
    }
    return result;
  }

  private static boolean allDefsInFragment(int[] defs, LinkedHashSet<Integer> fragmentInstructions) {
    for (int def : defs) {
      if (!fragmentInstructions.contains(def)) return false;
    }

    return true;
  }

  private static boolean allProperDefsInFragment(int[] defs, int ref, LinkedHashSet<Integer> fragmentInstructions, int[] postorder) {
    for (int def : defs) {
      if (!fragmentInstructions.contains(def) && postorder[def] < postorder[ref]) return false;
    }

    return true;
  }


  private static boolean anyDefInFragment(int[] defs, LinkedHashSet<Integer> fragmentInstructions) {
    for (int def : defs) {
      if (fragmentInstructions.contains(def)) return true;
    }

    return false;
  }

  @Nullable
  private static LuaType getType(PsiElement element) {
    if (element instanceof LuaExpression) return ((LuaExpression) element).getLuaType();
    
    return null;
  }

  private static VariableInfo[] filterNonlocals(Map<String, VariableInfo> infos, LuaStatementElement place) {
    List<VariableInfo> result = new ArrayList<VariableInfo>();
//    for (Iterator<VariableInfo> iterator = infos.values().iterator(); iterator.hasNext();) {
//      VariableInfo info = iterator.next();
//      String name = info.getName();
//      LuaPsiElement property = ResolveUtil.resolveProperty(place, name);
//      if (property instanceof LuaVariable) iterator.remove();
//      else if (property instanceof LuaReferenceExpression) {
//        LuaMember member = PsiTreeUtil.getParentOfType(property, LuaMember.class);
//        if (member == null) continue;
//        else if (!member.hasModifierProperty(PsiModifier.STATIC)) {
//          if (member.getContainingClass() instanceof LuaScriptClass) {
//            //binding variable
//            continue;
//          }
//        }
//      }
//      if (ResolveUtil.resolveClass(place, name) == null) {
//        result.add(info);
//      }
//    }
    return result.toArray(new VariableInfo[result.size()]);
  }

  private static LinkedHashSet<Integer> getFragmentInstructions(LuaStatementElement first, LuaStatementElement last, Instruction[] flow) {
    LinkedHashSet<Integer> result = new LinkedHashSet<Integer>();
    for (Instruction instruction : flow) {
      if (isInFragment(instruction, first, last)) {
        result.add(instruction.num());
      }
    }
    return result;
  }

  private static boolean isInFragment(Instruction instruction, LuaStatementElement first, LuaStatementElement last) {
    final PsiElement element = instruction.getElement();
    if (element == null) return false;
    return isInFragment(first, last, element);
  }

  private static boolean isInFragment(LuaStatementElement first, LuaStatementElement last, PsiElement element) {
    final PsiElement parent = first.getParent();
    if (!PsiTreeUtil.isAncestor(parent, element, true)) return false;
    PsiElement run = element;
    while (run.getParent() != parent) run = run.getParent();
    return isBetween(first, last, run);
  }

  private static boolean isBetween(PsiElement first, PsiElement last, PsiElement run) {
    while (first != null && first != run) first = first.getNextSibling();
    if (first == null) return false;
    while (last != null && last != run) last = last.getPrevSibling();
    if (last == null) return false;

    return true;
  }

  private static LinkedHashSet<Integer> getReachable(final LinkedHashSet<Integer> fragmentInsns, final Instruction[] flow, TIntObjectHashMap<TIntHashSet> dfaResult, final int[] postorder) {
    final LinkedHashSet<Integer> result = new LinkedHashSet<Integer>();
    for (Instruction insn : flow) {
      if (insn instanceof ReadWriteVariableInstruction &&
          !((ReadWriteVariableInstruction) insn).isWrite()) {
        final int ref = insn.num();
        TIntHashSet defs = dfaResult.get(ref);
        defs.forEach(new TIntProcedure() {
          public boolean execute(int def) {
            if (fragmentInsns.contains(def)) {
              if (!fragmentInsns.contains(ref) || postorder[ref] < postorder[def]) {
                result.add(ref);
                return false;
              }
            }
            return true;
          }
        });
      }
    }

    return result;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  private static String dumpDfaResult(ArrayList<TIntObjectHashMap<TIntHashSet>> dfaResult, ReachingDefinitionsDfaInstance dfa) {
    final StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < dfaResult.size(); i++) {
      TIntObjectHashMap<TIntHashSet> map = dfaResult.get(i);
      buffer.append("At " + i + ":\n");
      map.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {
        public boolean execute(int i, TIntHashSet defs) {
          buffer.append(i).append(" -> ");
          defs.forEach(new TIntProcedure() {
            public boolean execute(int i) {
              buffer.append(i).append(" ");
              return true;
            }
          });
          return true;
        }
      });
      buffer.append("\n");
    }

    return buffer.toString();
  }

  private static class VariableInfoImpl implements VariableInfo {
    private
    @NotNull final String myName;
    private final PsiManager myManager;

    private
    @Nullable
    LuaType myType;

    VariableInfoImpl(@NotNull String name, PsiManager manager) {
      myName = name;
      myManager = manager;
    }

    @NotNull
    public String getName() {
      return myName;
    }

    @Nullable
    public LuaType getType() {
      //if (myType instanceof PsiIntersectionType) return ((PsiIntersectionType) myType).getConjuncts()[0];
      return myType;
    }

    void addSubtype(LuaType t) {
//      if (t != null) {
//        if (myType == null) myType = t;
//        else {
//          if (!myType.isAssignableFrom(t)) {
//            if (t.isAssignableFrom(myType)) {
//              myType = t;
//            } else {
//              //TODO myType = TypesUtil.getLeastUpperBound(myType, t, myManager);
//            }
//          }
//        }
//      }
    }
  }

  private static TIntObjectHashMap<TIntHashSet> postprocess(final ArrayList<TIntObjectHashMap<TIntHashSet>> dfaResult, Instruction[] flow, ReachingDefinitionsDfaInstance dfaInstance) {
    TIntObjectHashMap<TIntHashSet> result = new TIntObjectHashMap<TIntHashSet>();
    for (int i = 0; i < flow.length; i++) {
      Instruction insn = flow[i];
      if (insn instanceof ReadWriteVariableInstruction) {
        ReadWriteVariableInstruction rwInsn = (ReadWriteVariableInstruction) insn;
        if (!rwInsn.isWrite()) {
          int idx = dfaInstance.getVarIndex(rwInsn.getVariableName());
          TIntHashSet defs = dfaResult.get(i).get(idx);
          if (defs == null) defs = new TIntHashSet();
          result.put(i, defs);
        }
      }
    }
    return result;
  }
}
