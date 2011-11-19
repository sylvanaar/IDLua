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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaFunctionDefinition;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.AfterCallInstruction;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.CallEnvironment;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.CallInstruction;
import com.sylvanaar.idea.Lua.lang.psi.controlFlow.Instruction;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaConditionalExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaUnaryExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaCompoundReferenceElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.statements.*;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaRecursiveElementVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;


/**
 * @author ven
 */
public class ControlFlowBuilder extends LuaRecursiveElementVisitor {
  private static final Logger log = Logger.getInstance("Lua.ControlFlowBuilder");

  private List<InstructionImpl> myInstructions;

  private Stack<InstructionImpl> myProcessingStack;
  //private final PsiConstantEvaluationHelper myConstantEvaluator;

    public ControlFlowBuilder(Project project) {
        // myConstantEvaluator = JavaPsiFacade.getInstance(project).getConstantEvaluationHelper();

    }

    private InstructionImpl myHead;
    private boolean myNegate;
    private boolean myAssertionsOnly;
    private LuaPsiElement myLastInScope;

    private List<Pair<InstructionImpl, LuaPsiElement>> myPending;

    private int myInstructionNumber;

    public void visitBlock(LuaBlock block) {
        final PsiElement parent = block.getParent();
        if (parent instanceof LuaFunctionDefinition) {
            final LuaParameter[] parameters = ((LuaFunctionDefinition) parent).getParameters().getLuaParameters();
            for (LuaParameter parameter : parameters) {
                addNode(new ReadWriteVariableInstructionImpl(parameter, myInstructionNumber++));
            }
        }
        super.visitBlock(block);

        handlePossibleReturn(block);
    }

    private void handlePossibleReturn(LuaBlock block) {
        final LuaStatementElement[] statements = block.getStatements();
        for(int i=statements.length; i<0; i--)
            handlePossibleReturn(statements[i]);
    }

    private void handlePossibleReturn(LuaStatementElement last) {
        if (PsiTreeUtil.isAncestor(myLastInScope, last, false)) {
            final MaybeReturnInstruction instruction = new MaybeReturnInstruction((LuaExpression) last, myInstructionNumber++);
            checkPending(instruction);
            addNode(instruction);
        }
    }

    final Object lock = new Object();
    
    public Instruction[] buildControlFlow(LuaPsiElement scope) {
        myInstructions = new ArrayList<InstructionImpl>();
        myProcessingStack = new Stack<InstructionImpl>();
        myPending = new ArrayList<Pair<InstructionImpl, LuaPsiElement>>();
        myInstructionNumber = 0;

        myLastInScope = null;

        if (scope instanceof LuaBlock) {
            LuaStatementElement[] statements = ((LuaPsiFile) scope).getStatements();
            if (statements.length > 0) {
                myLastInScope = statements[statements.length - 1];
            }
        }

        log.debug("Scope: " + scope + " parent: " + scope.getParent());

        startNode(null);

        scope.accept(this);

        final InstructionImpl end = startNode(null);

        checkPending(end); //collect return edges

        synchronized (lock) {
        for(Instruction i : myInstructions)
            log.debug(i.toString());
        }

        return myInstructions.toArray(new Instruction[myInstructions.size()]);
    }

//  private void buildFlowForClosure(final LuaClosableBlock closure) {
//    for (LuaParameter parameter : closure.getParameters()) {
//      addNode(new ReadWriteVariableInstructionImpl(parameter, myInstructionNumber++));
//    }
//
//    final Set<String> names = new LinkedHashSet<String>();
//
//    closure.accept(new LuaRecursiveElementVisitor() {
//      public void visitReferenceExpression(LuaReferenceExpression refExpr) {
//        super.visitReferenceExpression(refExpr);
//        if (refExpr.getQualifierExpression() == null && !PsiUtil.isLValue(refExpr)) {
//          if (!(refExpr.getParent() instanceof LuaCall)) {
//            final String refName = refExpr.getReferenceName();
//            if (!hasDeclaredVariable(refName, closure, refExpr)) {
//              names.add(refName);
//            }
//          }
//        }
//      }
//    });
//
//    names.add("owner");
//
//    for (String name : names) {
//      addNode(new ReadWriteVariableInstructionImpl(name, closure.getLBrace(), myInstructionNumber++, true));
//    }
//
//    PsiElement child = closure.getFirstChild();
//    while (child != null) {
//      if (child instanceof LuaPsiElement) {
//        ((LuaPsiElement)child).accept(this);
//      }
//      child = child.getNextSibling();
//    }
//
//    final LuaStatement[] statements = closure.getStatements();
//    if (statements.length > 0) {
//      handlePossibleReturn(statements[statements.length - 1]);
//    }
//  }

  private void addNode(InstructionImpl instruction) {
    myInstructions.add(instruction);
    if (myHead != null) {
      addEdge(myHead, instruction);
    }
    myHead = instruction;
  }

  static void addEdge(InstructionImpl beg, InstructionImpl end) {
    if (!beg.mySucc.contains(end)) {
      beg.mySucc.add(end);
    }

    if (!end.myPred.contains(beg)) {
      end.myPred.add(beg);
    }
  }

  public void visitFunctionDef(LuaFunctionDefinitionStatement e) {
    //do not go into functions

      e.getIdentifier().accept(this);
   //   addNode(new ReadWriteVariableInstructionImpl(e.getIdentifier(), myInstructionNumber++));
  }

    @Override
    public void visitDeclarationStatement(LuaDeclarationStatement e) {
        super.visitDeclarationStatement(e);

        for (LuaSymbol s : e.getDefinedSymbols())
            addNode(new ReadWriteVariableInstructionImpl(s, myInstructionNumber++));
    }


//    @Override
//    public void visitDeclarationStatement(LuaDeclarationStatement e) {
//        e.getDefinedSymbols().accept(this);
//    }
//
//    @Override
//    public void visitDeclarationExpression(LuaDeclarationExpression e) {
//        addNode(new ReadWriteVariableInstructionImpl(e, myInstructionNumber++));
//    }

    @Override
    public void visitFile(PsiFile file) {
        visitBlock((LuaBlock) file);
    }

    @Override
    public void visitDoStatement(LuaDoStatement e) {
        final InstructionImpl instruction = startNode(e);
        final LuaBlock body = e.getBlock();
        if (body != null) {
            body.accept(this);
        }
        finishNode(instruction);
    }

    //
//  public void visitBreakStatement(LuaBreakStatement breakStatement) {
//    super.visitBreakStatement(breakStatement);
//    final LuaStatementElement target = breakStatement.findTargetStatement();
//    if (target != null && myHead != null) {
//      addPendingEdge(target, myHead);
//    }
//
//    flowAbrupted();
//  }
//

  public void visitReturnStatement(LuaReturnStatement returnStatement) {
    boolean isNodeNeeded = myHead == null || myHead.getElement() != returnStatement;
    final LuaExpression value = returnStatement.getReturnValue();
    if (value != null) value.accept(this);

    if (isNodeNeeded) {
      InstructionImpl retInsn = startNode(returnStatement);
      addPendingEdge(null, myHead);
      finishNode(retInsn);
    }
    else {
      addPendingEdge(null, myHead);
    }
    flowAbrupted();
  }
//
//  public void visitAssertStatement(LuaAssertStatement assertStatement) {
//    final LuaExpression assertion = assertStatement.getAssertion();
//    if (assertion != null) {
//      assertion.accept(this);
//      final InstructionImpl assertInstruction = startNode(assertStatement);
//      final PsiType type = TypesUtil.createTypeByFQClassName("java.lang.AssertionError", assertStatement);
//      ExceptionInfo info = findCatch(type);
//      if (info != null) {
//        info.myThrowers.add(assertInstruction);
//      }
//      else {
//        addPendingEdge(null, assertInstruction);
//      }
//      finishNode(assertInstruction);
//    }
//  }
//
//
  private void flowAbrupted() {
    myHead = null;
  }

  public void visitAssignment(LuaAssignmentStatement e) {
    LuaIdentifierList lValues = e.getLeftExprs();
    LuaExpressionList rValues = e.getRightExprs();
    if (rValues != null) {
      rValues.accept(this);
      lValues.accept(this);
    }
  }

//  @Override
//  public void visitParenthesizedExpression(LuaParenthesizedExpression expression) {
//    final LuaExpression operand = expression.getOperand();
//    if (operand != null) operand.accept(this);
//  }
//
  @Override
  public void visitUnaryExpression(LuaUnaryExpression expression) {
    final LuaExpression operand = expression.getOperand();
    if (operand != null) {
      final boolean negation = expression.getOperationTokenType() == LuaElementTypes.NOT;
      if (negation) {
        myNegate = !myNegate;
      }
      operand.accept(this);
      if (negation) {
        myNegate = !myNegate;
      }
    }
  }

    @Override
    public void visitCompoundReference(LuaCompoundReferenceElementImpl e) {
        visitReferenceElement(e);
    }

    

    public void visitReferenceElement(LuaReferenceElement referenceExpression) {
    super.visitReferenceElement(referenceExpression);

    final ReadWriteVariableInstructionImpl i =
      new ReadWriteVariableInstructionImpl(referenceExpression, myInstructionNumber++,
              !myAssertionsOnly && LuaPsiUtils.isLValue(referenceExpression));
    addNode(i);
    checkPending(i);
  }

  public void visitIfThenStatement(LuaIfThenStatement ifStatement) {
    InstructionImpl ifInstruction = startNode(ifStatement);
    final LuaExpression condition = ifStatement.getIfCondition();

    final InstructionImpl head = myHead;
    final LuaBlock thenBranch = ifStatement.getIfBlock();
    InstructionImpl thenEnd = null;
    if (thenBranch != null) {
      if (condition != null) {
        condition.accept(this);
      }
      thenBranch.accept(this);
      handlePossibleReturn(thenBranch);
      thenEnd = myHead;
    }

    myHead = head;
    final LuaBlock elseBranch = ifStatement.getElseBlock();
    InstructionImpl elseEnd = null;
    if (elseBranch != null) {
      if (condition != null) {
        myNegate = !myNegate;
        final boolean old = myAssertionsOnly;
        myAssertionsOnly = true;
        condition.accept(this);
        myNegate = !myNegate;
        myAssertionsOnly = old;
      }

      elseBranch.accept(this);
      handlePossibleReturn(elseBranch);
      elseEnd = myHead;
    }


    if (thenBranch != null || elseBranch != null) {
      final InstructionImpl end = new IfEndInstruction(ifStatement, myInstructionNumber++);
      addNode(end);
      if (thenEnd != null) addEdge(thenEnd, end);
      if (elseEnd != null) addEdge(elseEnd, end);
    }
    finishNode(ifInstruction);



    /*InstructionImpl ifInstruction = startNode(ifStatement);
    final LuaCondition condition = ifStatement.getCondition();

    final InstructionImpl head = myHead;
    final LuaStatement thenBranch = ifStatement.getThenBranch();
    if (thenBranch != null) {
      if (condition != null) {
        condition.accept(this);
      }
      thenBranch.accept(this);
      handlePossibleReturn(thenBranch);
      addPendingEdge(ifStatement, myHead);
    }

    myHead = head;
    if (condition != null) {
      myNegate = !myNegate;
      final boolean old = myAssertionsOnly;
      myAssertionsOnly = true;
      condition.accept(this);
      myNegate = !myNegate;
      myAssertionsOnly = old;
    }

    final LuaStatement elseBranch = ifStatement.getElseBranch();
    if (elseBranch != null) {
      elseBranch.accept(this);
      handlePossibleReturn(elseBranch);
      addPendingEdge(ifStatement, myHead);
    }

    finishNode(ifInstruction);*/
  }
//
//  public void visitForStatement(LuaForStatement forStatement) {
//    final LuaForClause clause = forStatement.getClause();
//    if (clause instanceof LuaTraditionalForClause) {
//      for (LuaCondition initializer : ((LuaTraditionalForClause)clause).getInitialization()) {
//        initializer.accept(this);
//      }
//    }
//    else if (clause instanceof LuaForInClause) {
//      final LuaExpression expression = ((LuaForInClause)clause).getIteratedExpression();
//      if (expression != null) {
//        expression.accept(this);
//      }
//      for (LuaVariable variable : clause.getDeclaredVariables()) {
//        ReadWriteVariableInstructionImpl writeInsn = new ReadWriteVariableInstructionImpl(variable, myInstructionNumber++);
//        checkPending(writeInsn);
//        addNode(writeInsn);
//      }
//    }
//
//    InstructionImpl instruction = startNode(forStatement);
//    if (clause instanceof LuaTraditionalForClause) {
//      final LuaExpression condition = ((LuaTraditionalForClause)clause).getCondition();
//      if (condition != null) {
//        condition.accept(this);
//        if (!alwaysTrue(condition)) {
//          addPendingEdge(forStatement, myHead); //break cycle
//        }
//      }
//    } else {
//      addPendingEdge(forStatement, myHead); //break cycle
//    }
//
//    final LuaStatement body = forStatement.getBlock();
//    if (body != null) {
//      InstructionImpl bodyInstruction = startNode(body);
//      body.accept(this);
//      finishNode(bodyInstruction);
//    }
//    checkPending(instruction); //check for breaks targeted here
//
//    if (clause instanceof LuaTraditionalForClause) {
//      for (LuaExpression expression : ((LuaTraditionalForClause)clause).getUpdate()) {
//        expression.accept(this);
//      }
//    }
//    if (myHead != null) addEdge(myHead, instruction);  //loop
//    flowAbrupted();
//
//    finishNode(instruction);
//  }
//
  private void checkPending(InstructionImpl instruction) {
    final PsiElement element = instruction.getElement();
    if (element == null) {
      //add all
      for (Pair<InstructionImpl, LuaPsiElement> pair : myPending) {
        addEdge(pair.getFirst(), instruction);
      }
      myPending.clear();
    }
    else {
      for (int i = myPending.size() - 1; i >= 0; i--) {
        final Pair<InstructionImpl, LuaPsiElement> pair = myPending.get(i);
        final PsiElement scopeWhenToAdd = pair.getSecond();
        if (scopeWhenToAdd == null) continue;
        if (!PsiTreeUtil.isAncestor(scopeWhenToAdd, element, false)) {
          addEdge(pair.getFirst(), instruction);
          myPending.remove(i);
        }
        else {
          break;
        }
      }
    }
  }

  //add edge when instruction.getElement() is not contained in scopeWhenAdded
  private void addPendingEdge(LuaPsiElement scopeWhenAdded, InstructionImpl instruction) {
    if (instruction == null) return;

    int i = 0;
    if (scopeWhenAdded != null) {
      for (; i < myPending.size(); i++) {
        Pair<InstructionImpl, LuaPsiElement> pair = myPending.get(i);

        final LuaPsiElement currScope = pair.getSecond();
        if (currScope == null) continue;
        if (!PsiTreeUtil.isAncestor(currScope, scopeWhenAdded, true)) break;
      }
    }
    myPending.add(i, new Pair<InstructionImpl, LuaPsiElement>(instruction, scopeWhenAdded));
  }

  public void visitWhileStatement(LuaWhileStatement whileStatement) {
    final InstructionImpl instruction = startNode(whileStatement);
    final LuaConditionalExpression condition = whileStatement.getCondition();
    if (condition != null) {
      condition.accept(this);
    }
    if (!alwaysTrue(condition)) {
      addPendingEdge(whileStatement, myHead); //break
    }
    final LuaBlock body = whileStatement.getBlock();
    if (body != null) {
      body.accept(this);
    }
    checkPending(instruction); //check for breaks targeted here
    if (myHead != null) addEdge(myHead, instruction); //loop
    flowAbrupted();
    finishNode(instruction);
  }

  private boolean alwaysTrue(LuaExpression condition) {
      LuaType type = condition.getLuaType();

      if (type != LuaType.NIL && type != LuaType.BOOLEAN && type != LuaType.ANY)
          return true;

      return false;
  }


  private InstructionImpl startNode(@Nullable LuaPsiElement element) {
    return startNode(element, true);
  }

  private InstructionImpl startNode(LuaPsiElement element, boolean checkPending) {
    final InstructionImpl instruction = new InstructionImpl(element, myInstructionNumber++);
    addNode(instruction);
    if (checkPending) checkPending(instruction);
    return myProcessingStack.push(instruction);
  }

  private void finishNode(InstructionImpl instruction) {
    assert instruction.equals(myProcessingStack.pop());
/*    myHead = myProcessingStack.peek();*/
  }
//
//  public void visitField(LuaField field) {
//  }
//
//  public void visitParameter(LuaParameter parameter) {
//  }
//
//  public void visitMethod(LuaMethod method) {
//  }
//
//  public void visitTypeDefinition(LuaTypeDefinition typeDefinition) {
//    if (typeDefinition instanceof LuaAnonymousClassDefinition) {
//      super.visitTypeDefinition(typeDefinition);
//    }
//  }
//
//  public void visitVariable(LuaVariable variable) {
//    super.visitVariable(variable);
//    if (variable.getInitializerLua() != null ||
//        variable.getParent() instanceof LuaTupleDeclaration && ((LuaTupleDeclaration)variable.getParent()).getInitializerLua() != null) {
//      ReadWriteVariableInstructionImpl writeInsn = new ReadWriteVariableInstructionImpl(variable, myInstructionNumber++);
//      checkPending(writeInsn);
//      addNode(writeInsn);
//    }
//  }
//
  @Nullable
  private InstructionImpl findInstruction(PsiElement element) {
    for (int i = myProcessingStack.size() - 1; i >= 0; i--) {
      InstructionImpl instruction = myProcessingStack.get(i);
      if (element.equals(instruction.getElement())) return instruction;
    }
    return null;
  }

  static class CallInstructionImpl extends InstructionImpl implements CallInstruction {
    private final InstructionImpl myCallee;

    public String toString() {
      return super.toString() + " CALL " + myCallee.num();
    }

    public Iterable<? extends Instruction> succ(CallEnvironment env) {
      getStack(env, myCallee).push(this);
      return Collections.singletonList(myCallee);
    }

    public Iterable<? extends Instruction> allSucc() {
      return Collections.singletonList(myCallee);
    }

    protected String getElementPresentation() {
      return "";
    }

    CallInstructionImpl(int num, InstructionImpl callee) {
      super(null, num);
      myCallee = callee;
    }
  }

  static class PostCallInstructionImpl extends InstructionImpl implements AfterCallInstruction {
    private final CallInstructionImpl myCall;
    private RetInstruction myReturnInsn;

    public String toString() {
      return super.toString() + "AFTER CALL " + myCall.num();
    }

    public Iterable<? extends Instruction> allPred() {
      return Collections.singletonList(myReturnInsn);
    }

    public Iterable<? extends Instruction> pred(CallEnvironment env) {
      getStack(env, myReturnInsn).push(myCall);
      return Collections.singletonList(myReturnInsn);
    }

    protected String getElementPresentation() {
      return "";
    }

    PostCallInstructionImpl(int num, CallInstructionImpl call) {
      super(null, num);
      myCall = call;
    }

    public void setReturnInstruction(RetInstruction retInstruction) {
      myReturnInsn = retInstruction;
    }
  }

  static class RetInstruction extends InstructionImpl {
    RetInstruction(int num) {
      super(null, num);
    }

    public String toString() {
      return super.toString() + " RETURN";
    }

    protected String getElementPresentation() {
      return "";
    }

    public Iterable<? extends Instruction> succ(CallEnvironment env) {
      final Stack<CallInstruction> callStack = getStack(env, this);
      if (callStack.isEmpty()) return Collections.emptyList();     //can be true in case env was not populated (e.g. by DFA)

      final CallInstruction callInstruction = callStack.peek();
      final List<InstructionImpl> succ = ((CallInstructionImpl)callInstruction).mySucc;
      final Stack<CallInstruction> copy = (Stack<CallInstruction>)callStack.clone();
      copy.pop();
      for (InstructionImpl instruction : succ) {
        env.update(copy, instruction);
      }

      return succ;
    }
  }

  private static boolean hasDeclaredVariable(String name, LuaBlock scope, PsiElement place) {
    PsiElement prev = null;
    while (place != null) {
      if (place instanceof LuaBlock) {
        LuaStatementElement[] statements = ((LuaBlock)place).getStatements();
        for (LuaStatementElement statement : statements) {
          if (statement == prev) break;
          if (statement instanceof LuaDeclarationStatement) {
            LuaSymbol[] variables = ((LuaDeclarationStatement)statement).getDefinedSymbols();
            for (LuaSymbol variable : variables) {
              if (name.equals(variable.getName())) return true;
            }
          }
        }
      }

      if (place == scope) {
        break;
      }
      else {
        prev = place;
        place = place.getParent();
      }
    }

    return false;
  }


}
