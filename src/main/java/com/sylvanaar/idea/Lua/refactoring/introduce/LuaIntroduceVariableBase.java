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

package com.sylvanaar.idea.Lua.refactoring.introduce;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElementFactory;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFileBase;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaAssignmentStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaConditionalLoop;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDeclarationStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaStatementOwner;
import com.sylvanaar.idea.Lua.refactoring.LuaRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author ilyas
 */
public abstract class LuaIntroduceVariableBase extends LuaIntroduceHandlerBase<LuaIntroduceVariableSettings> {

  private static final Logger log =
    Logger.getInstance("Lua.IntroduceVariableBase");
  protected static String REFACTORING_NAME = "Introduce Variable";
    public static final String IDLUAREFACTORTMP = "_______IDLUAREFACTORTMP";
    private PsiElement positionElement = null;

  @NotNull
  @Override
  protected PsiElement findScope(LuaExpression selectedExpr, LuaSymbol variable) {
      final PsiElement container = LuaRefactoringUtil.getEnclosingContainer(selectedExpr);

      if (container == null)
          throw new LuaIntroduceRefactoringError("Refactoring is not supported in this context");

      return container;
  }

  protected void checkExpression(LuaExpression selectedExpr) {
    // Cannot perform refactoring in parameter default values

    PsiElement parent = selectedExpr.getParent();
    while (!(parent == null || parent instanceof LuaPsiFileBase || parent instanceof LuaParameter)) {
      parent = parent.getParent();
    }

    if (parent instanceof LuaParameter) {
      throw new LuaIntroduceRefactoringError("Refactoring is not supported in parameters");
    }
  }

  @Override
  protected void checkVariable(LuaSymbol variable) throws LuaIntroduceRefactoringError {
    if (variable instanceof LuaCompoundIdentifier && !((LuaCompoundIdentifier) variable).isCompoundDeclaration())
        return;

   throw new LuaIntroduceRefactoringError(null);
  }

  @Override
  protected void checkOccurrences(PsiElement[] occurrences) {
    //nothing to do
  }

//  private static boolean checkInFieldInitializer(LuaExpression expr) {
//    PsiElement parent = expr.getParent();
//    if (parent instanceof GrClosableBlock) {
//      return false;
//    }
//    if (parent instanceof GrField && expr == ((GrField)parent).getInitializerGroovy()) {
//      return true;
//    }
//    if (parent instanceof LuaExpression) {
//      return checkInFieldInitializer(((LuaExpression)parent));
//    }
//    return false;
//  }

  /**
   * Inserts new variable declaratrions and replaces occurrences
   */
  public LuaSymbol runRefactoring(final LuaIntroduceContext context, final LuaIntroduceVariableSettings settings) {
    // Generating varibable declaration

    final LuaPsiElementFactory factory = LuaPsiElementFactory.getInstance(context.project);

    LuaDeclarationStatement varDecl = null;

    if (context.var == null) {
        if (settings.isLocal())
            varDecl = (LuaDeclarationStatement) factory.createStatementFromText(
                    "local " + settings.getName() + " = " + context.expression.getText());
        else
            varDecl = (LuaDeclarationStatement) factory.createStatementFromText(
                    settings.getName() + " = " + context.expression.getText());
    } else {
        varDecl = (LuaDeclarationStatement) factory.createStatementFromText((settings.isLocal() ? "local " : "") + settings.getName() + " = " + IDLUAREFACTORTMP);

    }
//      = factory.createVariableDeclaration(settings.isDeclareFinal() ? new String[]{PsiModifier.FINAL} : null,
//                                 (LuaExpression)PsiUtil.skipParentheses(context.expression, false), settings.getSelectedType(),
//                                 settings.getName());

    // Marker for caret posiotion
    try {
        LuaExpression firstOccurrence;

      if (context.var == null) {
          /* insert new variable */
          LuaRefactoringUtil.sortOccurrences(context.occurrences);
          if (context.occurrences.length == 0 || !(context.occurrences[0] instanceof LuaExpression)) {
            throw new IncorrectOperationException("Wrong expression occurrence");
          }


      }
        
      if (settings.replaceAllOccurrences()) {
        firstOccurrence = ((LuaExpression)context.occurrences[0]);
      }
      else {
        firstOccurrence = context.expression;
      }

      //resolveLocalConflicts(context.scope, varDecl.getVariables()[0].getName());
      // Replace at the place of first occurrence

      LuaSymbol insertedVar = replaceFirstAssignmentStatement(firstOccurrence, context, varDecl);
      boolean alreadyDefined = insertedVar != null;
      if (insertedVar == null) {
        // Insert before first occurrence

        if (context.var != null)
            substituteInitializerExpression((LuaExpression) context.var.copy(), varDecl);

        assert varDecl.getDefinedSymbols().length > 0;

        insertedVar = insertVariableDefinition(context, settings, varDecl);
      }

//      insertedVar.setType(settings.getSelectedType());

      //Replace other occurrences
      LuaSymbol refExpr = createReferenceSymbol(settings, factory);
      if (settings.replaceAllOccurrences()) {
        ArrayList<PsiElement> replaced = new ArrayList<PsiElement>();
        for (PsiElement occurrence : context.occurrences) {
          if (!(alreadyDefined && firstOccurrence.equals(occurrence))) {
            if (occurrence instanceof LuaExpression) {
              LuaExpression element = (LuaExpression)occurrence;
              if (element.getParent() instanceof LuaReferenceElement)
                  element = (LuaExpression) element.getParent();

              replaced.add(element.replaceWithExpression(refExpr, true));
              // For caret position
              if (occurrence.equals(context.expression)) {
                refreshPositionMarker(replaced.get(replaced.size() - 1));
              }
              refExpr = createReferenceSymbol(settings, factory);
            }
            else {
              throw new IncorrectOperationException("Expression occurrence to be replaced is not instance of GroovyPsiElement");
            }
          }
        }
        if (context.editor != null) {
          // todo implement it...
//              final PsiElement[] replacedOccurrences = replaced.toArray(new PsiElement[replaced.size()]);
//              highlightReplacedOccurrences(myProject, editor, replacedOccurrences);
        }
      }
      else {
        if (!alreadyDefined) {
          refreshPositionMarker(context.expression.replaceWithExpression(refExpr, true));
        }
      }


      // Setting caret to logical position
      if (context.editor != null && getPositionMarker() != null) {
        context.editor.getCaretModel().moveToOffset(getPositionMarker().getTextRange().getEndOffset());
        context.editor.getSelectionModel().removeSelection();
      }
      return insertedVar;
    }
    catch (IncorrectOperationException e) {
      log.error(e);
    }
    return null;
  }

    private void substituteInitializerExpression(LuaExpression expression, LuaDeclarationStatement varDecl) {
        int markerPos = varDecl.getText().indexOf(IDLUAREFACTORTMP);
        LuaExpression fakeInitializer = PsiTreeUtil
                .findElementOfClassAtOffset(varDecl.getContainingFile(), markerPos, LuaExpression.class, false);

        assert fakeInitializer.getText().equals(IDLUAREFACTORTMP);

        if (fakeInitializer instanceof LuaExpressionList)
            fakeInitializer = ((LuaExpressionList) fakeInitializer).getLuaExpressions().get(0);
        
        if (fakeInitializer.getParent() instanceof LuaReferenceElement)
            fakeInitializer = (LuaExpression) fakeInitializer.getParent();

        fakeInitializer.replace(expression);
    }

    private LuaSymbol createReferenceSymbol(LuaIntroduceVariableSettings settings, LuaPsiElementFactory factory) {
        LuaSymbol symbol = settings.isLocal() ? factory.createLocalNameIdentifier(settings.getName()) :
        factory.createGlobalNameIdentifier(settings.getName());

        if (! (symbol instanceof LuaReferenceElement))
            symbol = (LuaSymbol) symbol.getParent();

        return symbol;
    }

    private static void resolveLocalConflicts(PsiElement tempContainer, String varName) {
    for (PsiElement child : tempContainer.getChildren()) {
//      if (child instanceof LuaReferenceElement && !child.getText().contains(".")) {
//        PsiReference psiReference = child.getReference();
//        if (psiReference != null) {
//          final PsiElement resolved = psiReference.resolve();
//          if (resolved != null) {
//            String fieldName = getFieldName(resolved);
//            if (fieldName != null && varName.equals(fieldName)) {
//              LuaPsiElementFactory factory = LuaPsiElementFactory.getInstance(tempContainer.getProject());
//              ((LuaReferenceElement)child).replaceWithExpression(factory.createExpressionFromText("this." + child.getText()), true);
//            }
//          }
//        }
//      }
//      else {
        resolveLocalConflicts(child, varName);
//      }
    }
  }

//  @Nullable
//  private static String getFieldName(PsiElement element) {
//    if (element instanceof GrAccessorMethod) element = ((GrAccessorMethod)element).getProperty();
//    return element instanceof GrField ? ((GrField)element).getName() : null;
//  }

  private void refreshPositionMarker(PsiElement position) {
    if (positionElement == null && position != null) {
      positionElement = position;
    }
  }

  private PsiElement getPositionMarker() {
    return positionElement;
  }

  private LuaSymbol insertVariableDefinition(LuaIntroduceContext context,
                                              LuaIntroduceVariableSettings settings,
                                              LuaDeclarationStatement varDecl) throws IncorrectOperationException {
    log.assertTrue(context.occurrences.length > 0);

    LuaStatementElement anchorElement = (LuaStatementElement)findAnchor(context, settings, context.occurrences, context.scope);
    log.assertTrue(anchorElement != null);
    PsiElement realContainer = anchorElement.getParent();

    assert LuaRefactoringUtil.isAppropriateContainerForIntroduceVariable(realContainer);

    if (!(realContainer instanceof LuaConditionalLoop)) {
      if (realContainer instanceof LuaStatementOwner) {
        LuaStatementOwner block = (LuaStatementOwner)realContainer;
        varDecl = (LuaDeclarationStatement)block.addStatementBefore(varDecl, anchorElement);

        block.addAfter(LuaPsiElementFactory.getInstance(context.project).createWhiteSpaceFromText("\n"), varDecl);
      }
    }
//    else {
//      // To replace branch body correctly
//      String refId = varDecl.getVariables()[0].getName();
//      GrBlockStatement newBody;
//      final GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(context.project);
//      if (anchorElement.equals(context.expression)) {
//        newBody = factory.createBlockStatement(varDecl);
//      }
//      else {
//        replaceExpressionOccurrencesInStatement(anchorElement, context.expression, refId, settings.replaceAllOccurrences());
//        newBody = factory.createBlockStatement(varDecl, anchorElement);
//      }
//
//      varDecl = (LuaSymbolDeclaration)newBody.getBlock().getStatements()[0];
//
//      GrCodeBlock tempBlock = ((GrBlockStatement)((GrLoopStatement)realContainer).replaceBody(newBody)).getBlock();
//      refreshPositionMarker(tempBlock.getStatements()[tempBlock.getStatements().length - 1]);
//    }

    return varDecl.getDefinedSymbols()[0];
  }

  private static void replaceExpressionOccurrencesInStatement(LuaStatementElement stmt,
                                                              LuaExpression expr,
                                                              String refText,
                                                              boolean replaceAllOccurrences)
    throws IncorrectOperationException {
    LuaPsiElementFactory factory = LuaPsiElementFactory.getInstance(stmt.getProject());
    LuaExpression refExpr = factory.createExpressionFromText(refText);
    if (!replaceAllOccurrences) {
      expr.replaceWithExpression(refExpr, true);
    }
    else {
      PsiElement[] occurrences = LuaRefactoringUtil.getExpressionOccurrences(expr, stmt);
      for (PsiElement occurrence : occurrences) {
        if (occurrence instanceof LuaExpression) {
          LuaExpression LuaExpression = (LuaExpression)occurrence;
          LuaExpression.replaceWithExpression(refExpr, true);
          refExpr = factory.createExpressionFromText(refText);
        }
        else {
          throw new IncorrectOperationException();
        }
      }
    }
  }

    /**
     * Replaces an expression occurrence by appropriate variable declaration
     */
    @Nullable
    private LuaSymbol replaceFirstAssignmentStatement(@NotNull LuaExpression expr, LuaIntroduceContext context,
                                                      @NotNull LuaDeclarationStatement definition) throws
            IncorrectOperationException {


        LuaAssignmentStatement assign =
                PsiTreeUtil.getParentOfType(expr, LuaAssignmentStatement.class, true, LuaStatementElement.class);
        if (assign != null) {
            LuaSymbol symbol = assign.getLeftExprs().getSymbols()[0];
            if (symbol instanceof LuaReferenceElement)
                symbol = (LuaSymbol) ((LuaReferenceElement) symbol).getElement();
            
            boolean isSymbolAssinedto = false;
            for (PsiElement element : context.occurrences)
                if (element.equals(symbol)) isSymbolAssinedto = true;

            if (isSymbolAssinedto) {
                // for now we only support single assignments when we are replacing an assignment with a variable
                // definition
                if (assign.getLeftExprs().count() != 1 && assign.getRightExprs().count() != 1) return null;

                substituteInitializerExpression(
                        (LuaExpression) assign.getRightExprs().getLuaExpressions().get(0).copy(), definition);

                definition = (LuaDeclarationStatement) assign.replaceWithStatement(definition);

                if (expr.equals(context.expression)) {
                    refreshPositionMarker(definition);
                }
                return definition.getDefinedSymbols()[0];
            }
        }
        return null;
    }
}
