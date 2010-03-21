//package com.sylvanaar.idea.Lua.psi.impl;
//
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.editor.Document;
//import com.intellij.openapi.fileEditor.FileDocumentManager;
//import com.intellij.openapi.util.TextRange;
//import com.intellij.psi.AbstractElementManipulator;
//import com.intellij.psi.PsiDocumentManager;
//import com.intellij.util.IncorrectOperationException;
////import com.sylvanaar.idea.Lua.configurator.LuaServersConfiguration;
//import com.sylvanaar.idea.Lua.psi.LuaDirectiveValue;
//
///**
// * Created by IntelliJ IDEA.
// * User: Max
// * Date: 24.08.2009
// * Time: 14:48:06
// */
//public class LuaDirectiveValueManipulator extends AbstractElementManipulator<LuaDirectiveValue> {
//
////    /**
////     * Some included file name has been changed. Changing value text and rebuilding configuration file types mapping
////     */
////    public LuaDirectiveValue handleContentChange(LuaDirectiveValue element, TextRange range, String newContent) throws IncorrectOperationException {
////
////        String oldText = element.getText();
////        String newText = oldText.substring(0, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset());
////        Document document = FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
////        document.replaceString(element.getTextRange().getStartOffset(), element.getTextRange().getEndOffset(), newText);
////        PsiDocumentManager.getInstance(element.getProject()).commitDocument(document);
////
////        LuaServersConfiguration LuaServersConfiguration = ApplicationManager.getApplication().getComponent(LuaServersConfiguration.class);
////        LuaServersConfiguration.rebuildFilepaths();
////
////        return element;
////
////    }
//}
