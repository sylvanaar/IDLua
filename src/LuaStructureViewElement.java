///*
// * Copyright 2010 Jon S Akhtar (Sylvanaar)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package com.sylvanaar.idea.Lua;
//
//import com.intellij.ide.structureView.StructureViewTreeElement;
//import com.intellij.navigation.ItemPresentation;
//import com.intellij.navigation.NavigationItem;
//import com.intellij.openapi.editor.colors.TextAttributesKey;
//import com.intellij.openapi.util.Iconable;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.PsiNamedElement;
//import com.intellij.psi.util.PsiTreeUtil;
//import com.sylvanaar.idea.Lua.psi.LuaPsiElement;
//
//import javax.swing.*;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by IntelliJ IDEA.
// * User: Jon S Akhtar
// * Date: Apr 10, 2010
// * Time: 3:32:17 PM
// */
//public class LuaStructureViewElement implements StructureViewTreeElement {
//    private LuaPsiElement element;
//
//    public LuaStructureViewElement(LuaPsiElement element) {
//        this.element = element;
//    }
//
//    @Override
//    public Object getValue() {
//        return element;
//    }
//
//    @Override
//    public void navigate(boolean requestFocus) {
//        ((NavigationItem)element).navigate(requestFocus);
//    }
//
//    @Override
//    public boolean canNavigate() {
//       return ((NavigationItem)element).canNavigate();
//    }
//
//    @Override
//    public boolean canNavigateToSource() {
//        return ((NavigationItem)element).canNavigateToSource();
//    }
//               @Override
//        public StructureViewTreeElement[] getChildren() {
//          final List<LuaPsiElement> childrenElements = new ArrayList<LuaPsiElement>();
//          element.acceptChildren(new LuaElementVisitor() {
//            public void visitElement(PsiElement element) {
//              if (element instanceof PsiNamedElement && ((PsiNamedElement)element).getName() != null) {
//                childrenElements.add((LuaPsiElement)element);
//              }
//              else {
//                element.acceptChildren(this);
//              }
//            }
//
//            public void visitLuaVariable(final LuaVariable node) {
//              // Do not add local variables to structure view.
//              if (PsiTreeUtil.getParentOfType(node, LuaFunction.class) == null) {
//                super.visitLuaVariable(node);
//              }
//            }
//
//            public void visitJSParameter(final LuaParameter node) {
//              // Do not add parameters to structure view
//            }
//
//            public void visitJSObjectLiteralExpression(final LuaObjectLiteralExpression node) {
//              childrenElements.add(node);
//            }
//          });
//
//          StructureViewTreeElement[] children = new StructureViewTreeElement[childrenElements.size()];
//          for (int i = 0; i < children.length; i++) {
//            children[i] = new LuaStructureViewElement(childrenElements.get(i));
//          }
//
//          return children;
//        }
//              @Override
//        public ItemPresentation getPresentation() {
//          return new ItemPresentation() {
//            public String getPresentableText() {
//              if (element instanceof JSObjectLiteralExpression) {
//                if (element.getParent() instanceof JSAssignmentExpression) {
//                  return ((JSAssignmentExpression)element.getParent()).getLOperand().getText();
//                }
//                else {
//                  return "prototype";
//                }
//              }
//              return ((PsiNamedElement)element).getName();
//            }
//
//            public TextAttributesKey getTextAttributesKey() {
//              return null;
//            }
//
//            public String getLocationString() {
//              return null;
//            }
//
//            public Icon getIcon(boolean open) {
//              return element.getIcon(Iconable.ICON_FLAG_OPEN);
//            }
//          };
//        }
//
//}
