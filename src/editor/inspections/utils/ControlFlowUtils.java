/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or aLuaeed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.sylvanaar.idea.Lua.editor.inspections.utils;

import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaConditionalExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings({"OverlyComplexClass"})
public class ControlFlowUtils {

  private ControlFlowUtils() {
    super();
  }

    public static boolean statementMayCompleteNormally(
            @Nullable LuaStatementElement statement) {
        if (statement == null) {
            return true;
        }
        if (statement instanceof LuaBreakStatement ||
                statement instanceof LuaReturnStatement) {
            return false;
        }

        if (statement instanceof LuaGenericForStatement || statement instanceof LuaNumericForStatement) {
            return forStatementMayReturnNormally(statement);
        }

        if (statement instanceof LuaWhileStatement) {
            return whileStatementMayReturnNormally(
                    (LuaWhileStatement) statement);
        }

        if (statement instanceof LuaDoStatement) {
            return blockMayCompleteNormally(((LuaDoStatement) statement).getBlock());
        }

        if (statement instanceof LuaBlock) {
            return blockMayCompleteNormally((LuaBlock) statement);
        }

        if (statement instanceof LuaIfThenStatement) {
            return ifStatementMayReturnNormally((LuaIfThenStatement) statement);
        }
        return true;
    }

    private static boolean whileStatementMayReturnNormally(
      @NotNull LuaWhileStatement loopStatement) {
    final LuaConditionalExpression test = loopStatement.getCondition();
    return (!BoolUtils.isTrue(test)
        || statementIsBreakTarget(loopStatement)) ;
  }

  private static boolean forStatementMayReturnNormally(
      @NotNull LuaStatementElement loopStatement) {
    return true;
  }


  private static boolean ifStatementMayReturnNormally(
      @NotNull LuaIfThenStatement ifStatement) {
    final LuaBlock thenBranch = ifStatement.getIfBlock();
    if (blockMayCompleteNormally(thenBranch)) {
      return true;
    }
    final LuaBlock elseBranch = ifStatement.getElseBlock();
    return elseBranch == null ||
        blockMayCompleteNormally(elseBranch);
  }

  public static boolean blockMayCompleteNormally(
      @Nullable LuaBlock block) {
    if (block == null) {
      return true;
    }

    final LuaStatementElement[] statements = block.getStatements();
    for (final LuaStatementElement statement : statements) {
      if (!statementMayCompleteNormally(statement)) {
        return false;
      }
    }
    return true;
  }


  private static boolean statementIsBreakTarget(
      @NotNull LuaStatementElement statement) {
    final BreakFinder breakFinder = new BreakFinder(statement);
    statement.accept(breakFinder);
    return breakFinder.breakFound();
  }

  public static boolean statementContainsReturn(
      @NotNull LuaStatementElement statement) {
    final ReturnFinder returnFinder = new ReturnFinder();
    statement.accept(returnFinder);
    return returnFinder.returnFound();
  }

  public static boolean isInLoop(@NotNull LuaPsiElement element) {
    final LuaConditionalLoop loop =
        PsiTreeUtil.getParentOfType(element, LuaConditionalLoop.class);
    if (loop == null) {
      return false;
    }
    final LuaBlock body = loop.getBlock();
    return PsiTreeUtil.isAncestor(body, element, true);
  }



//  private static boolean isInWhileStatementBody(@NotNull LuaPsiElement element) {
//    final LuaWhileStatement whileStatement =
//        PsiTreeUtil.getParentOfType(element, LuaWhileStatement.class);
//    if (whileStatement == null) {
//      return false;
//    }
//    final LuaStatementElement body = whileStatement.getBlock();
//    return PsiTreeUtil.isAncestor(body, element, true);
//  }

//  private static boolean isInForStatementBody(@NotNull LuaPsiElement element) {
//    final LuaForStatement forStatement =
//        PsiTreeUtil.getParentOfType(element, LuaForStatement.class);
//    if (forStatement == null) {
//      return false;
//    }
//    final LuaStatementElement body = forStatement.getBlock();
//    return PsiTreeUtil.isAncestor(body, element, true);
//  }


//  public static LuaStatementElement stripBraces(@NotNull LuaStatementElement branch) {
//    if (branch instanceof LuaBlockStatement) {
//      final LuaBlockStatement block = (LuaBlockStatement) branch;
//      final LuaStatement[] statements = block.getBlock().getStatements();
//      if (statements.length == 1) {
//        return statements[0];
//      } else {
//        return block;
//      }
//    } else {
//      return branch;
//    }
//  }

  public static boolean statementCompletesWithStatement(
      @NotNull LuaStatementElement containingStatement,
      @NotNull LuaStatementElement statement) {
    LuaPsiElement statementToCheck = statement;
    while (true) {
      if (statementToCheck.equals(containingStatement)) {
        return true;
      }
      final LuaPsiElement container =
          getContainingStatement(statementToCheck);
      if (container == null) {
        return false;
      }
      if (container instanceof LuaBlock) {
        if (!statementIsLastInBlock((LuaBlock) container,
            (LuaStatementElement) statementToCheck)) {
          return false;
        }
      }
      if (isLoop(container)) {
        return false;
      }
      statementToCheck = container;
    }
  }

  public static boolean blockCompletesWithStatement(
      @NotNull LuaBlock body,
      @NotNull LuaStatementElement statement) {
    LuaStatementElement statementToCheck = statement;
    while (true) {
      if (statementToCheck == null) {
        return false;
      }
      final LuaStatementElement container =
          getContainingStatement(statementToCheck);
      if (container == null) {
        return false;
      }
      if (isLoop(container)) {
        return false;
      }
      if (container instanceof LuaBlock) {
        if (!statementIsLastInBlock((LuaBlock) container,
            statementToCheck)) {
          return false;
        }
        if (container.equals(body)) {
          return true;
        }
        statementToCheck =
            PsiTreeUtil.getParentOfType(container,
                LuaStatementElement.class);
      } else {
        statementToCheck = container;
      }
    }
  }


  private static boolean isLoop(@NotNull LuaPsiElement element) {
    return element instanceof LuaConditionalLoop;
  }

  @Nullable
  private static LuaStatementElement getContainingStatement(
      @NotNull LuaPsiElement statement) {
    return PsiTreeUtil.getParentOfType(statement, LuaStatementElement.class);
  }

  @Nullable
  private static LuaPsiElement getContainingStatementOrBlock(
      @NotNull LuaPsiElement statement) {
    return PsiTreeUtil.getParentOfType(statement, LuaStatementElement.class, LuaBlock.class);
  }

  private static boolean statementIsLastInBlock(@NotNull LuaBlock block,
                                                @NotNull LuaStatementElement statement) {
    final LuaStatementElement[] statements = block.getStatements();
    for (int i = statements.length - 1; i >= 0; i--) {
      final LuaStatementElement childStatement = statements[i];
      if (statement.equals(childStatement)) {
        return true;
      }
      if (!(childStatement instanceof LuaReturnStatement)) {
        return false;
      }
    }
    return false;
  }

  private static boolean statementIsLastInCodeBlock(@NotNull LuaBlock block,
                                                    @NotNull LuaStatementElement statement) {
    final LuaStatementElement[] statements = block.getStatements();
    for (int i = statements.length - 1; i >= 0; i--) {
      final LuaStatementElement childStatement = statements[i];
      if (statement.equals(childStatement)) {
        return true;
      }
      if (!(childStatement instanceof LuaReturnStatement)) {
        return false;
      }
    }
    return false;
  }

  private static class ReturnFinder extends LuaElementVisitor {
    private boolean m_found = false;

    public boolean returnFound() {
      return m_found;
    }

    public void visitReturnStatement(
        @NotNull LuaReturnStatement returnStatement) {
      if (m_found) {
        return;
      }
      super.visitReturnStatement(returnStatement);
      m_found = true;
    }
  }

  private static class BreakFinder extends LuaElementVisitor {
    private boolean m_found = false;
    private final LuaStatementElement m_target;

    private BreakFinder(@NotNull LuaStatementElement target) {
      super();
      m_target = target;
    }

    public boolean breakFound() {
      return m_found;
    }

    public void visitBreakStatement(
        @NotNull LuaBreakStatement breakStatement) {
      if (m_found) {
        return;
      }
      super.visitBreakStatement(breakStatement);
    // TODO
      final LuaStatementElement exitedStatement = null; // TODO breakStatement.findTargetStatement();
      if (exitedStatement == null) {
        return;
      }
      if (PsiTreeUtil.isAncestor(exitedStatement, m_target, false)) {
        m_found = true;
      }
    }
  }


//  public static boolean isInExitStatement(@NotNull LuaExpression expression) {
//    return isInReturnStatementArgument(expression) ||
//        isInThrowStatementArgument(expression);
//  }

//  private static boolean isInReturnStatementArgument(
//      @NotNull LuaExpression expression) {
//    final LuaReturnStatement returnStatement =
//        PsiTreeUtil
//            .getParentOfType(expression, LuaReturnStatement.class);
//    return returnStatement != null;
//  }

//  private static boolean isInThrowStatementArgument(
//      @NotNull LuaExpression expression) {
//    final LuaThrowStatement throwStatement =
//        PsiTreeUtil
//            .getParentOfType(expression, LuaThrowStatement.class);
//    return throwStatement != null;
//  }


//  public interface ExitPointVisitor {
//    boolean visit(Instruction instruction);
//  }
//
//  public static void visitAllExitPoints(@Nullable LuaBlock block, ExitPointVisitor visitor) {
//    if (block == null) return;
//    final Instruction[] flow = block.getControlFlow();
//    boolean[] visited = new boolean[flow.length];
//    visitAllExitPointsInner(flow[flow.length - 1], flow[0], visited, visitor);
//  }
//
//  private static boolean visitAllExitPointsInner(Instruction last, Instruction first, boolean[] visited, ExitPointVisitor visitor) {
//    if (first == last) return true;
//    if (last instanceof MaybeReturnInstruction) {
//      return visitor.visit(last);
//    }
//
//    final PsiElement element = last.getElement();
//    if (element != null) {
//      return visitor.visit(last);
//    }
//    visited[last.num()] = true;
//    for (Instruction pred : last.allPred()) {
//      if (!visited[pred.num()]) {
//        if (!visitAllExitPointsInner(pred, first, visited, visitor)) return false;
//      }
//    }
//    return true;
//  }
}
