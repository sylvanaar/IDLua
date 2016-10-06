///*
// * Copyright 2011 Jon S Akhtar (Sylvanaar)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.sylvanaar.idea.Lua.editor.completion.smartEnter.processors;
//
//import com.intellij.codeInsight.editorActions.smartEnter.EnterProcessor;
//import com.intellij.openapi.actionSystem.IdeActions;
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.editor.Editor;
//import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
//import com.intellij.openapi.editor.actionSystem.EditorActionManager;
//import com.intellij.openapi.editor.ex.EditorEx;
//import com.intellij.psi.PsiElement;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaConditionalLoop;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaIfThenStatement;
//import com.sylvanaar.idea.Lua.lang.psi.statements.LuaWhileStatement;
//
///**
// * User: Dmitry.Krasilschikov
// * Date: 05.08.2008
// */
//public class LuaPlainEnterProcessor implements EnterProcessor {
//  public boolean doEnter(Editor editor, PsiElement psiElement, boolean isModified) {
//    LuaBlock block = getControlStatementBlock(editor.getCaretModel().getOffset(), psiElement);
//
//    if (block != null) {
//      PsiElement firstChild = block.getFirstChild();
//      PsiElement firstElement = firstChild!=null?firstChild.getNextSibling():null;
//
//      editor.getCaretModel().moveToOffset(firstElement != null ?
//              firstElement.getTextRange().getStartOffset() - 1 :
//              block.getTextRange().getEndOffset());
//    }
//
//      final Editor edi = editor;
//      ApplicationManager.getApplication().runWriteAction(new Runnable() {
//          @Override
//          public void run() {
//              getEnterHandler().execute(edi, ((EditorEx) edi).getDataContext());
//          }
//      });
//
//      return true;
//  }
//
//  private EditorActionHandler getEnterHandler() {
//    EditorActionHandler enterHandler = EditorActionManager.getInstance().getActionHandler(
//            IdeActions.ACTION_EDITOR_START_NEW_LINE
//    );
//    return enterHandler;
//  }
//
//  private LuaBlock getControlStatementBlock(int caret, PsiElement element) {
//    LuaBlock body = null;
//    if (element instanceof LuaIfThenStatement) {
//      body = ((LuaIfThenStatement) element).getIfBlock();
//      if (caret > body.getTextRange().getEndOffset()) {
//        body = ((LuaIfThenStatement) element).getElseBlock();
//      }
//    } else if (element instanceof LuaWhileStatement) {
//      body = ((LuaWhileStatement) element).getBlock();
//    } else if (element instanceof LuaConditionalLoop) {
//      body = ((LuaConditionalLoop) element).getBlock();
//    }
//
//    return body;
//  }
//}
