package com.sylvanaar.idea.Lua.lang.formatter.processors;

import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.formatter.blocks.LuaFormattingBlock;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;

import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.NEWLINE;
import static com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes.SHORTCOMMENT;


public class LuaSpacingProcessor extends LuaElementVisitor {
  private static final ThreadLocal<LuaSpacingProcessor> mySharedProcessorAllocator = new ThreadLocal<LuaSpacingProcessor>();
  protected MyLuaSpacingVisitor myLuaElementVisitor;
  protected static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.lua.formatter.processors.LuaSpacingProcessor");

  private LuaSpacingProcessor(MyLuaSpacingVisitor visitor) {
//    super(visitor);
    myLuaElementVisitor = visitor;
  }

  public static Spacing getSpacing(LuaFormattingBlock child1, LuaFormattingBlock child2, CodeStyleSettings settings) {
    return getSpacing(child2.getNode(), settings);
  }

  private static Spacing getSpacing(ASTNode node, CodeStyleSettings settings) {
    LuaSpacingProcessor spacingProcessor = mySharedProcessorAllocator.get();
    try {
      if (spacingProcessor == null) {
        spacingProcessor = new LuaSpacingProcessor(new MyLuaSpacingVisitor(node, settings));
        mySharedProcessorAllocator.set(spacingProcessor);
      } else {
        spacingProcessor.setVisitor(new MyLuaSpacingVisitor(node, settings));
      }
      spacingProcessor.doInit();
      return spacingProcessor.getResult();
    }
    catch (Exception e) {
      LOG.error(e);
      return null;
    }
    finally {
      spacingProcessor.clear();
    }
  }


  private void doInit() {
    myLuaElementVisitor.doInit();
  }

  private void clear() {
    if (myLuaElementVisitor != null) {
      myLuaElementVisitor.clear();
    }
  }

  private Spacing getResult() {
    return myLuaElementVisitor.getResult();
  }

  public void setVisitor(MyLuaSpacingVisitor visitor) {
    myLuaElementVisitor = visitor;
  }

  /**
   * Visitor to adjust spaces via user Code Style Settings
   */
  private static class MyLuaSpacingVisitor extends LuaElementVisitor {
    private PsiElement myParent;
    private final CodeStyleSettings mySettings;

    private Spacing myResult;
    private ASTNode myChild1;
    private ASTNode myChild2;

    public MyLuaSpacingVisitor(ASTNode node, CodeStyleSettings settings) {
      mySettings = settings;
      init(node);
    }

    private void init(final ASTNode child) {
      if (child == null) return;
      ASTNode treePrev = child.getTreePrev();
      while (treePrev != null && SpacingUtil.isWhiteSpace(treePrev)) {
        treePrev = treePrev.getTreePrev();
      }
      if (treePrev == null) {
        init(child.getTreeParent());
      } else {
        myChild2 = child;
        myChild1 = treePrev;
        final CompositeElement parent = (CompositeElement) treePrev.getTreeParent();
        myParent = SourceTreeToPsiMap.treeElementToPsi(parent);
      }
    }

    /*
    Method to start visiting
     */
    private void doInit() {
      if (myChild1 == null || myChild2 == null) return;
      PsiElement psi1 = myChild1.getPsi();
      PsiElement psi2 = myChild2.getPsi();
      if (psi1 == null || psi2 == null) return;
      if (psi1.getLanguage() != LuaFileType.LUA_LANGUAGE ||
              psi2.getLanguage() != LuaFileType.LUA_LANGUAGE) {
        return;
      }

      if (myChild2 != null && mySettings.KEEP_FIRST_COLUMN_COMMENT && SpacingUtil.COMMENT_BIT_SET.contains(myChild2.getElementType())) {

          myResult = Spacing.createKeepingFirstColumnSpacing(0, Integer.MAX_VALUE, true, 1);

        return;
      }

      if (myChild1 != null && myChild2 != null && myChild1.getElementType() == NEWLINE) {
        final ASTNode prev = SpacingUtil.getPrevElementType(myChild1);
        if (prev != null && prev.getElementType() == SHORTCOMMENT) {
          myResult = Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
          return;
        }
      }

      if (myParent instanceof LuaPsiElement) {
        ((LuaPsiElement) myParent).accept(this);
      }
    }
//  }
//    @Override
//    public void visitAnnotation(GrAnnotation annotation) {
//      if (myChild2.getElementType() == ANNOTATION_ARGUMENTS) {
//        myResult = Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
////      }
////    }
//
//    public void visitArgumentList(GrArgumentList list) {
//      if (myChild1.getElementType() == mLBRACK || myChild2.getElementType() == mRBRACK) {
//        createSpaceInCode(mySettings.SPACE_WITHIN_BRACKETS);
//      }
//      // todo add other cases
//    }
//
//    @Override
//    public void visitMethodCallExpression(GrMethodCallExpression methodCallExpression) {
//      if (myChild2.getElementType() == ARGUMENTS) createSpaceInCode(mySettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES);
//    }
//
//    public void visitClosure(GrClosableBlock closure) {
//      if ((myChild1.getElementType() == mLCURLY && myChild2.getElementType() != PARAMETERS_LIST && myChild2.getElementType() != mCLOSABLE_BLOCK_OP)
//              || myChild2.getElementType() == mRCURLY) {
//        myResult = Spacing.createDependentLFSpacing(0, Integer.MAX_VALUE, closure.getTextRange(), mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      } else if (myChild1.getElementType() == mCLOSABLE_BLOCK_OP) {
//        GrStatement[] statements = closure.getStatements();
//        if (statements.length > 0) {
//          TextRange range = new TextRange(statements[0].getTextRange().getStartOffset(), statements[statements.length - 1].getTextRange().getEndOffset());
//          myResult = Spacing.createDependentLFSpacing(1, Integer.MAX_VALUE, range, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//        }
//      }
//    }
//
//    public void visitOpenBlock(GrOpenBlock block) {
//      if (myChild1.getElementType() == mLCURLY && myChild2.getElementType() == mRCURLY && block.getParent() instanceof GrBlockStatement) {
//        myResult = Spacing.createSpacing(1, 1, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      } else
//      if (myChild1.getElementType() == mLCURLY && !LuaEditorActionUtil.isMultilineStringElement(myChild2) ||
//              myChild2.getElementType() == mRCURLY && !LuaEditorActionUtil.isMultilineStringElement(myChild1)) {
//        myResult = Spacing.createDependentLFSpacing(0, 1, block.getTextRange(), mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      }
//    }
//
////    public void visitNewExpression(GrNewExpression newExpression) {
////      if (myChild1.getElementType() == kNEW) {
////        createSpaceInCode(true);
////      } else if (myChild2.getElementType() == ARGUMENTS) {
////        createSpaceInCode(mySettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES);
////      }
////    }
//
//    public void visitTypeDefinition(GrTypeDefinition typeDefinition) {
//      if (myChild2.getElementType() == CLASS_BODY) {
//        PsiIdentifier nameIdentifier = typeDefinition.getNameIdentifier();
//        int dependanceStart = nameIdentifier == null ? myParent.getTextRange().getStartOffset() : nameIdentifier.getTextRange().getStartOffset();
//        myResult = getSpaceBeforeLBrace(mySettings.SPACE_BEFORE_CLASS_LBRACE, mySettings.CLASS_BRACE_STYLE,
//                new TextRange(dependanceStart, myChild1.getTextRange().getEndOffset()), false);
//      }
//    }
//
//    public void visitTypeDefinitionBody(GrTypeDefinitionBody typeDefinitionBody) {
//      if (myChild1.getElementType() == mLCURLY && myChild2.getElementType() == mRCURLY) {
//        myResult = Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_BEFORE_RBRACE);
//      } else if (myChild1.getElementType() == mLCURLY) {
//        myResult = Spacing.createSpacing(0, 0, mySettings.BLANK_LINES_AFTER_CLASS_HEADER + 1,
//                mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_DECLARATIONS);
//      } else if (myChild2.getElementType() == mRCURLY) {
//        myResult = Spacing.createSpacing(0, Integer.MAX_VALUE, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_BEFORE_RBRACE);
//      }
//    }
//
//    public void visitMethod(GrMethod method) {
//      if (myChild2.getElementType() == mLPAREN) {
//        createSpaceInCode(mySettings.SPACE_BEFORE_METHOD_PARENTHESES);
//      } else if (myChild2.getElementType() == mRPAREN && myChild2.getElementType() == THROW_CLAUSE) {
//        createSpaceInCode(true);
//      } else if (myChild2.getElementType() == OPEN_BLOCK) {
//        PsiElement methodName = method.getNameIdentifier();
//        int dependancyStart = methodName == null ? myParent.getTextRange().getStartOffset() : methodName.getTextRange().getStartOffset();
//        myResult = getSpaceBeforeLBrace(mySettings.SPACE_BEFORE_METHOD_LBRACE, mySettings.METHOD_BRACE_STYLE,
//                new TextRange(dependancyStart, myChild1.getTextRange().getEndOffset()), mySettings.KEEP_SIMPLE_METHODS_IN_ONE_LINE);
//      } else if (myChild1.getElementType() == MODIFIERS) {
//        processModifierList(myChild1);
//      } else if (COMMENT_SET.contains(myChild1.getElementType())
//              && (myChild2.getElementType() == MODIFIERS || myChild2.getElementType() == REFERENCE_ELEMENT)) {
//        myResult = Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, 0);
//      }
//
//    }
//
//    public void visitDocMethodReference(GrDocMethodReference reference) {
//      visitDocMember();
//    }
//
//
//
//
//    public void visitWhileStatement(LuaWhileStatement statement) {
//      if (myChild2.getElementType() == LPAREN) {
//        createSpaceInCode(mySettings.SPACE_BEFORE_WHILE_PARENTHESES);
//      } else if (myChild1.getElementType() == LPAREN || myChild2.getElementType() == RPAREN) {
//        createSpaceInCode(mySettings.SPACE_WITHIN_WHILE_PARENTHESES);
//      } else if (myChild2.getPsi() instanceof GrBlockStatement) {
//        myResult = getSpaceBeforeLBrace(mySettings.SPACE_BEFORE_WHILE_LBRACE, mySettings.BRACE_STYLE,
//                new TextRange(myParent.getTextRange().getStartOffset(), myChild1.getTextRange().getEndOffset()), mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE);
//      } else {
//        createSpacingBeforeElementInsideControlStatement();
//      }
//    }
//
//
//
//
//
//    public void visitIfStatement(GrIfStatement ifStatement) {
//      if (myChild2.getElementType() == kELSE) {
//        if (myChild1.getElementType() != OPEN_BLOCK && myChild1.getElementType() != BLOCK_STATEMENT) {
//          myResult = Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//        } else {
//          if (mySettings.ELSE_ON_NEW_LINE) {
//            myResult = Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//          } else {
//            createSpaceProperty(true, false, 0);
//          }
//        }
//      } else if (myChild1.getElementType() == kELSE) {
//        if (myChild2.getElementType() == IF_STATEMENT) {
//          if (mySettings.SPECIAL_ELSE_IF_TREATMENT) {
//            createSpaceProperty(true, false, 0);
//          } else {
//            myResult = Spacing.createSpacing(1, 1, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//          }
//        } else {
//          if (myChild2.getElementType() == BLOCK_STATEMENT || myChild2.getElementType() == OPEN_BLOCK) {
//            myResult = getSpaceBeforeLBrace(mySettings.SPACE_BEFORE_ELSE_LBRACE, mySettings.BRACE_STYLE,
//                    null,
//                    mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE);
//          } else {
//            createSpacingBeforeElementInsideControlStatement();
//          }
//        }
//      } else if (myChild2.getElementType() == BLOCK_STATEMENT || myChild2.getElementType() == OPEN_BLOCK) {
//        boolean space = myChild2.getPsi() == ((GrIfStatement) myParent).getElseBranch() ? mySettings.SPACE_BEFORE_ELSE_LBRACE : mySettings.SPACE_BEFORE_IF_LBRACE;
//        myResult = getSpaceBeforeLBrace(space, mySettings.BRACE_STYLE, new TextRange(myParent.getTextRange().getStartOffset(),
//                myChild1.getTextRange().getEndOffset()),
//                mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE);
//      } else if (myChild2.getElementType() == mLPAREN) {
//        createSpaceInCode(mySettings.SPACE_BEFORE_IF_PARENTHESES);
//      } else if (myChild1.getElementType() == mLPAREN) {
//        createSpaceInCode(mySettings.SPACE_WITHIN_IF_PARENTHESES);
//      } else if (myChild2.getElementType() == mRPAREN) {
//        createSpaceInCode(mySettings.SPACE_WITHIN_IF_PARENTHESES);
//      } else if (((GrIfStatement) myParent).getThenBranch() == myChild2.getPsi()) {
//        createSpacingBeforeElementInsideControlStatement();
//      }
//    }
//
//    public void visitForStatement(GrForStatement forStatement) {
//      if (myChild2.getElementType() == mLPAREN) {
//        createSpaceInCode(mySettings.SPACE_BEFORE_FOR_PARENTHESES);
//      } else if (myChild1.getElementType() == mLPAREN) {
//        ASTNode rparenth = findFrom(myChild2, mRPAREN, true);
//        if (rparenth == null) {
//          createSpaceInCode(mySettings.SPACE_WITHIN_FOR_PARENTHESES);
//        } else {
//          createParenSpace(mySettings.FOR_STATEMENT_LPAREN_ON_NEXT_LINE, mySettings.SPACE_WITHIN_FOR_PARENTHESES,
//                  new TextRange(myChild1.getTextRange().getStartOffset(), rparenth.getTextRange().getEndOffset()));
//        }
//      } else if (myChild2.getElementType() == mRPAREN) {
//        ASTNode lparenth = findFrom(myChild2, mLPAREN, false);
//        if (lparenth == null) {
//          createSpaceInCode(mySettings.SPACE_WITHIN_FOR_PARENTHESES);
//        } else {
//          createParenSpace(mySettings.FOR_STATEMENT_RPAREN_ON_NEXT_LINE, mySettings.SPACE_WITHIN_FOR_PARENTHESES,
//                  new TextRange(lparenth.getTextRange().getStartOffset(), myChild2.getTextRange().getEndOffset()));
//        }
//
//      } else if (myChild2.getElementType() == BLOCK_STATEMENT || myChild2.getElementType() == OPEN_BLOCK) {
//        if (myChild2.getElementType() == BLOCK_STATEMENT) {
//          myResult = getSpaceBeforeLBrace(mySettings.SPACE_BEFORE_FOR_LBRACE, mySettings.BRACE_STYLE,
//                  new TextRange(myParent.getTextRange().getStartOffset(), myChild1.getTextRange().getEndOffset()), mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE);
//        } else if (mySettings.KEEP_CONTROL_STATEMENT_IN_ONE_LINE) {
//          myResult = Spacing.createDependentLFSpacing(1, 1, myParent.getTextRange(), false, mySettings.KEEP_BLANK_LINES_IN_CODE);
//        } else {
//          myResult = Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
//        }
//      }
//    }
//
//    private void createParenSpace(final boolean onNewLine, final boolean space) {
//      createParenSpace(onNewLine, space, myParent.getTextRange());
//    }
//
//    private void createParenSpace(final boolean onNewLine, final boolean space, final TextRange dependance) {
//      if (onNewLine) {
//        final int spaces = space ? 1 : 0;
//        myResult = Spacing
//                .createDependentLFSpacing(spaces, spaces, dependance, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      } else {
//        createSpaceInCode(space);
//      }
//    }
//
//
//    private static ASTNode findFrom(ASTNode current, final IElementType expected, boolean forward) {
//      while (current != null) {
//        if (current.getElementType() == expected) return current;
//        current = forward ? current.getTreeNext() : current.getTreePrev();
//      }
//      return null;
//    }
//
//
//    private void processOnNewLineCondition(final boolean onNewLine) {
//      if (onNewLine) {
//        if (!mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
//          myResult = Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//        } else {
//          myResult = Spacing.createDependentLFSpacing(0, 1, myParent.getTextRange(), mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//        }
//      } else {
//        createSpaceProperty(true, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      }
//    }
//
//
//    private void createSpacingBeforeElementInsideControlStatement() {
//      if (mySettings.KEEP_CONTROL_STATEMENT_IN_ONE_LINE && myChild1.getElementType() != mSL_COMMENT) {
//        createSpaceProperty(true, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      } else {
//        myResult = Spacing.createSpacing(1, 1, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      }
//    }
//
//
//    private void processModifierList(ASTNode modifierList) {
//      if (modifierList.getLastChildNode().getElementType() == ANNOTATION && mySettings.METHOD_ANNOTATION_WRAP == CodeStyleSettings.WRAP_ALWAYS) {
//        myResult = Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      }
//      else if (mySettings.MODIFIER_LIST_WRAP) {
//        myResult = Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      }
//      else {
//        createSpaceProperty(true, false, 0);
//      }
//    }
//
//
    protected void clear() {
      myResult = null;
      myChild2 = myChild1 = null;
      myParent = null;
    }

    protected Spacing getResult() {
      final Spacing result = myResult;
      clear();
      return result;
    }
//
//    private void createSpaceInCode(final boolean space) {
//      createSpaceProperty(space, mySettings.KEEP_BLANK_LINES_IN_CODE);
//    }
//
//    private void createSpaceProperty(boolean space, int keepBlankLines) {
//      createSpaceProperty(space, mySettings.KEEP_LINE_BREAKS, keepBlankLines);
//    }
//
//    private void createSpaceProperty(boolean space, boolean keepLineBreaks, final int keepBlankLines) {
//      final ASTNode prev = SpacingUtil.getPrevElementType(myChild2);
//      if (prev != null && prev.getElementType() == mSL_COMMENT) {
//        myResult = Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      } else {
//        myResult = Spacing.createSpacing(space ? 1 : 0, space ? 1 : 0, 0, keepLineBreaks, keepBlankLines);
//      }
//    }
//
//    private Spacing getSpaceBeforeLBrace(final boolean spaceBeforeLbrace, int braceStyle, TextRange dependantRange, boolean keepOneLine) {
//      if (dependantRange != null && braceStyle == CodeStyleSettings.NEXT_LINE_IF_WRAPPED) {
//        int space = spaceBeforeLbrace ? 1 : 0;
//        return createNonLFSpace(space, dependantRange, false);
//      } else if (braceStyle == CodeStyleSettings.END_OF_LINE || braceStyle == CodeStyleSettings.NEXT_LINE_IF_WRAPPED) {
//        int space = spaceBeforeLbrace ? 1 : 0;
//        return createNonLFSpace(space, null, false);
//      } else if (keepOneLine) {
//        int space = spaceBeforeLbrace ? 1 : 0;
//        return Spacing.createDependentLFSpacing(space, space, myParent.getTextRange(), mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      } else {
//        return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      }
//    }
//
//    private Spacing createNonLFSpace(int spaces, final TextRange dependantRange, final boolean keepLineBreaks) {
//      final ASTNode prev = SpacingUtil.getPrevElementType(myChild2);
//      if (prev != null && prev.getElementType() == mSL_COMMENT) {
//        return Spacing.createSpacing(0, Integer.MAX_VALUE, 1, keepLineBreaks, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      } else if (dependantRange != null) {
//        return Spacing.createDependentLFSpacing(spaces, spaces, dependantRange, keepLineBreaks, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      } else {
//        return Spacing.createSpacing(spaces, spaces, 0, keepLineBreaks, mySettings.KEEP_BLANK_LINES_IN_CODE);
//      }
//    }
//
//
  }
}
