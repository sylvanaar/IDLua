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

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pass;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.IntroduceTargetChooser;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.Function;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFileBase;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaExpressionList;
import com.sylvanaar.idea.Lua.lang.psi.lists.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.impl.PsiUtil;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDeclarationStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaGenericForStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaIfThenStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaWhileStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.refactoring.LuaRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim.Medvedev
 */
public abstract class LuaIntroduceHandlerBase<Settings extends LuaIntroduceSettings> implements RefactoringActionHandler {
  protected abstract String getRefactoringName();

  protected abstract String getHelpID();

  @NotNull
  protected abstract PsiElement findScope(LuaExpression expression, LuaSymbol variable);

  protected abstract void checkExpression(LuaExpression selectedExpr) throws LuaIntroduceRefactoringError;

  protected abstract void checkVariable(LuaSymbol variable) throws LuaIntroduceRefactoringError;

  protected abstract void checkOccurrences(PsiElement[] occurrences);

  protected abstract LuaIntroduceDialog<Settings> getDialog(LuaIntroduceContext context);

  @Nullable
  public abstract LuaSymbol runRefactoring(LuaIntroduceContext context, Settings settings);

  public static List<LuaExpression> collectExpressions(final PsiFile file, final Editor editor, final int offset) {
    int correctedOffset = correctOffset(editor, offset);
    final PsiElement elementAtCaret = file.findElementAt(correctedOffset);
    final List<LuaExpression> expressions = new ArrayList<LuaExpression>();

    for (LuaExpression expression = PsiTreeUtil.getParentOfType(elementAtCaret, LuaExpression.class);
         expression != null;
         expression = PsiTreeUtil.getParentOfType(expression, LuaExpression.class)) {
      if (expressions.contains(expression)) continue;
      if (expressionIsNotCorrect(expression)) continue;

      expressions.add(expression);
    }
    return expressions;
  }

    private static boolean expressionIsNotCorrect(final LuaExpression expression) {
        return expression instanceof LuaReferenceElement ||
                expression instanceof LuaExpressionList || expression instanceof LuaIdentifierList;

    }

    private static int correctOffset(Editor editor, int offset) {
    Document document = editor.getDocument();
    CharSequence text = document.getCharsSequence();
    int correctedOffset = offset;
    int textLength = document.getTextLength();
    if (offset >= textLength) {
      correctedOffset = textLength - 1;
    }
    else if (!Character.isJavaIdentifierPart(text.charAt(offset))) {
      correctedOffset--;
    }
    if (correctedOffset < 0) {
      correctedOffset = offset;
    }
    else if (!Character.isJavaIdentifierPart(text.charAt(correctedOffset))) {
      if (text.charAt(correctedOffset) == ';') {//initially caret on the end of line
        correctedOffset--;
      }
      if (text.charAt(correctedOffset) != ')') {
        correctedOffset = offset;
      }
    }
    return correctedOffset;
  }

  @Nullable
  private static LuaSymbol findVariableAtCaret(final PsiFile file, final Editor editor, final int offset) {
    final int correctOffset = correctOffset(editor, offset);
    final PsiElement elementAtCaret = file.findElementAt(correctOffset);
    final LuaSymbol variable = PsiTreeUtil.getParentOfType(elementAtCaret, LuaSymbol.class);
    if (variable != null && variable.getTextRange().contains(correctOffset)) return variable;
    return null;
  }

  public void invoke(final @NotNull Project project, final Editor editor, final PsiFile file, final @Nullable DataContext dataContext) {
    final SelectionModel selectionModel = editor.getSelectionModel();
    if (!selectionModel.hasSelection()) {
      final int offset = editor.getCaretModel().getOffset();

      final List<LuaExpression> expressions = collectExpressions(file, editor, offset);
      if (expressions.isEmpty()) {
        final LuaSymbol variable = findVariableAtCaret(file, editor, offset);
        if (variable == null || variable instanceof LuaParameter) {
          selectionModel.selectLineAtCaret();
        }
        else {
          final TextRange textRange = variable.getTextRange();
          selectionModel.setSelection(textRange.getStartOffset(), textRange.getEndOffset());
        }
      }
      else if (expressions.size() == 1) {
        final TextRange textRange = expressions.get(0).getTextRange();
        selectionModel.setSelection(textRange.getStartOffset(), textRange.getEndOffset());
      }
      else {
        IntroduceTargetChooser.showChooser(editor, expressions,
                                           new Pass<LuaExpression>() {
                                             public void pass(final LuaExpression selectedValue) {
                                               invoke(project, editor, file, selectedValue.getTextRange().getStartOffset(),
                                                      selectedValue.getTextRange().getEndOffset());
                                             }
                                           },
                                           new Function<LuaExpression, String>() {
                                             @Override
                                             public String fun(LuaExpression LuaExpression) {
                                               return LuaExpression.getText();
                                             }
                                           });
        return;
      }
    }
    invoke(project, editor, file, selectionModel.getSelectionStart(), selectionModel.getSelectionEnd());
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
    // Does nothing
  }

  public LuaIntroduceContext getContext(Project project, Editor editor, LuaExpression expression, @Nullable LuaSymbol variable) {
    final PsiElement scope = findScope(expression, variable);

    if (variable == null) {
      final PsiElement[] occurences = findOccurences(expression, scope);
      return new LuaIntroduceContext(project, editor, expression, occurences, scope, variable);

    }
    else {
//      final List<PsiElement> list = Collections.synchronizedList(new ArrayList<PsiElement>());
//      ReferencesSearch.search(variable, new LocalSearchScope(scope)).forEach(new Processor<PsiReference>() {
//        @Override
//        public boolean process(PsiReference psiReference) {
//          final PsiElement element = psiReference.getElement();
//          if (element != null) {
//            list.add(element);
//          }
//          return true;
//        }
//      });
        final PsiElement[] occurences = findOccurences(variable, scope);
//      return new LuaIntroduceContext(project, editor, variable, list.toArray(new PsiElement[list.size()]), scope,
//                                    variable);
      return new LuaIntroduceContext(project, editor, variable, occurences, scope,
                                    variable);
    }
  }

    protected PsiElement[] findOccurences(LuaExpression expression, PsiElement scope) {
        final PsiElement expr = PsiUtil.skipParentheses(expression, false);
        assert expr != null;
        final PsiElement[] occurrences = LuaRefactoringUtil.getExpressionOccurrences(expr, scope);
        if (occurrences == null || occurrences.length == 0) {
            throw new LuaIntroduceRefactoringError("No occurances found");
        }
        return occurrences;
    }

  private boolean invoke(final Project project, final Editor editor, PsiFile file, int startOffset, int endOffset) {
    try {
      PsiDocumentManager.getInstance(project).commitAllDocuments();
      if (!(file instanceof LuaPsiFileBase)) {
        throw new LuaIntroduceRefactoringError("Only Lua files");
      }
      if (!CommonRefactoringUtil.checkReadOnlyStatus(project, file)) {
        throw new LuaIntroduceRefactoringError("Read-only occurances found");
      }

      LuaExpression selectedExpr = findExpression((LuaPsiFileBase)file, startOffset, endOffset);
      final LuaSymbol variable = findVariable((LuaPsiFile)file, startOffset, endOffset);
      if (variable != null) {
        checkVariable(variable);
      }
      else if (selectedExpr != null) {
        checkExpression(selectedExpr);
      }
      else {
        throw new LuaIntroduceRefactoringError(null);
      }

      final LuaIntroduceContext context = getContext(project, editor, selectedExpr, variable);
      checkOccurrences(context.occurrences);
      final Settings settings = showDialog(context);
      if (settings == null) return false;

      CommandProcessor.getInstance().executeCommand(context.project, new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Computable<LuaSymbol>() {
          public LuaSymbol compute() {
            return runRefactoring(context, settings);
          }
        });
      }
    }, getRefactoringName(), null);

      return true;
    }
    catch (LuaIntroduceRefactoringError e) {
      CommonRefactoringUtil
        .showErrorHint(project, editor, RefactoringBundle.getCannotRefactorMessage(e.getMessage()), getRefactoringName(), getHelpID());
      return false;
    }
  }

  @Nullable
  private static LuaSymbol findVariable(LuaPsiFile file, int startOffset, int endOffset) {
    LuaSymbol var = LuaRefactoringUtil.findElementInRange(file, startOffset, endOffset, LuaSymbol.class);
    if (var == null) {
      final LuaDeclarationStatement variableDeclaration =
        LuaRefactoringUtil.findElementInRange(file, startOffset, endOffset, LuaDeclarationStatement.class);
      if (variableDeclaration == null) return null;
      final LuaSymbol[] variables = variableDeclaration.getDefinedSymbols();
      if (variables.length == 1) {
        var = variables[0];
      }
    }
    if (var instanceof LuaParameter) {
      return null;
    }
    return var;
  }

  @Nullable
  public static LuaExpression findExpression(LuaPsiFileBase file, int startOffset, int endOffset) {
    LuaExpression selectedExpr = LuaRefactoringUtil.findElementInRange(file, startOffset, endOffset, LuaExpression.class);
    if (selectedExpr == null) return null;
//    PsiType type = selectedExpr.getType();
//    if (type != null) type = TypeConversionUtil.erasure(type);
//
//    if (PsiType.VOID.equals(type)) {
//      throw new GrIntroduceRefactoringError(LuaRefactoringBundle.message("selected.expression.has.void.type"));
//    }
//
//    if (expressionIsNotCorrect(selectedExpr)) {
//      throw new GrIntroduceRefactoringError(LuaRefactoringBundle.message("selected.block.should.represent.an.expression"));
//    }

    return selectedExpr;
  }

  @Nullable
  private Settings showDialog(LuaIntroduceContext context) {

    // Add occurences highlighting
    ArrayList<RangeHighlighter> highlighters = new ArrayList<RangeHighlighter>();
    HighlightManager highlightManager = null;
    if (context.editor != null) {
      highlightManager = HighlightManager.getInstance(context.project);
      EditorColorsManager colorsManager = EditorColorsManager.getInstance();
      TextAttributes attributes = colorsManager.getGlobalScheme().getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);
      if (context.occurrences.length > 1) {
        highlightManager.addOccurrenceHighlights(context.editor, context.occurrences, attributes, true, highlighters);
      }
    }

    LuaIntroduceDialog<Settings> dialog = getDialog(context);

    dialog.show();
    if (dialog.isOK()) {
      if (context.editor != null) {
        assert highlightManager != null : "highlight manager is null";
        for (RangeHighlighter highlighter : highlighters) {
          highlightManager.removeSegmentHighlighter(context.editor, highlighter);
        }
      }
      return dialog.getSettings();
    }
    else {
      if (context.occurrences.length > 1) {
        WindowManager.getInstance().getStatusBar(context.project)
          .setInfo("Press escape to remove highlighting");
      }
    }
    return null;
  }

  @Nullable
  public static PsiElement findAnchor(LuaIntroduceContext context,
                                       LuaIntroduceSettings settings,
                                       PsiElement[] occurrences,
                                       final PsiElement container) {
    if (occurrences.length == 0) return null;
    PsiElement candidate;
    if (occurrences.length == 1 || !settings.replaceAllOccurrences()) {
      candidate = context.expression;
    }
    else {
      LuaRefactoringUtil.sortOccurrences(occurrences);
      candidate = occurrences[0];
    }
    while (candidate != null && !container.equals(candidate.getParent())) {
      candidate = candidate.getParent();
    }
    if (candidate == null) {
      return null;
    }
    if ((container instanceof LuaWhileStatement) &&
        candidate.equals(((LuaWhileStatement)container).getCondition())) {
      return container;
    }
    if ((container instanceof LuaIfThenStatement) &&
        candidate.equals(((LuaIfThenStatement)container).getIfCondition())) {
      return container;
    }
    if ((container instanceof LuaGenericForStatement) &&
        candidate.equals(((LuaGenericForStatement)container).getInClause())) {
      return container;
    }
    return candidate;
  }

//  protected static void deleteLocalVar(LuaIntroduceContext context) {
//    final LuaSymbol resolved = GrIntroduceFieldHandler.resolveLocalVar(context);
//    final PsiElement parent = resolved.getParent();
//    if (parent instanceof GrTupleDeclaration) {
//      if (((GrTupleDeclaration)parent).getVariables().length == 1) {
//        parent.getParent().delete();
//      }
//      else {
//        final LuaExpression initializerLua = resolved.getInitializerLua();
//        if (initializerLua != null) initializerLua.delete();
//        resolved.delete();
//      }
//    }
//    else {
//      if (((LuaSymbolDeclaration)parent).getVariables().length == 1) {
//        parent.delete();
//      }
//      else {
//        resolved.delete();
//      }
//    }
//  }

  protected static LuaSymbol resolveLocalVar(LuaIntroduceContext context) {
    if (context.var != null) return context.var;
    return (LuaSymbol)((LuaReferenceElement)context.expression).resolve();
  }

//  public static boolean hasLhs(final PsiElement[] occurrences) {
//    for (PsiElement element : occurrences) {
//      if (element instanceof GrReferenceExpression) {
//        if (PsiUtil.isLValue((LuaPsiElement)element)) return true;
//        if (ControlFlowUtils.isIncOrDecOperand((GrReferenceExpression)element)) return true;
//      }
//    }
//    return false;
//  }
//
//
  public interface Validator  {
    boolean isOK(LuaIntroduceDialog dialog);
  }
}
