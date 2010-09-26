/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
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
package com.sylvanaar.idea.Lua.codeInsight;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.GutterIconTooltipHelper;
import com.intellij.codeInsight.daemon.impl.MarkerType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.util.NullableFunction;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;


import java.util.Collection;
import java.util.List;

/**
 * @author ilyas
 * Same logic as for Java LMP
 */
public class LuaLineMarkerProvider implements LineMarkerProvider, DumbAware {
  DaemonCodeAnalyzerSettings myDaemonSettings = null;
  EditorColorsManager myColorsManager = null;

  public LuaLineMarkerProvider(DaemonCodeAnalyzerSettings myDaemonSettings, EditorColorsManager myColorsManager) {
      this.myDaemonSettings = myDaemonSettings;
      this.myColorsManager = myColorsManager;
  }


    NullableFunction<PsiElement, String> tailCallTooltip = new NullableFunction<PsiElement, String>() {
        @Override
        public String fun(PsiElement psiElement) {
            return "Tail Call";
        }
    };

  @Override
  public LineMarkerInfo getLineMarkerInfo(final PsiElement element) {
      final PsiElement parent = element.getParent();

      if (element instanceof LuaReturnStatement) {
          LuaReturnStatement e = (LuaReturnStatement) element;

          if (e.isTailCall())
            return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), LuaIcons.TAIL_RECURSION, Pass.LINE_MARKERS,
                    tailCallTooltip, null,
                    GutterIconRenderer.Alignment.LEFT);              
      }

//    final PsiElement parent = element.getParent();
//    if (parent instanceof PsiNameIdentifierOwner) {
//      final ASTNode node = element.getNode();
//      if (node != null && TokenSets.PROPERTY_NAMES.contains(node.getElementType())) {
//        return super.getLineMarkerInfo(((PsiNameIdentifierOwner)parent).getNameIdentifier());
//      }
//    }
//    //need to draw method separator above docComment
//    if (myDaemonSettings.SHOW_METHOD_SEPARATORS && element.getFirstChild() == null) {
//      PsiElement element1 = element;
//      boolean isMember = false;
//      while (element1 != null && !(element1 instanceof PsiFile) && element1.getPrevSibling() == null) {
//        element1 = element1.getParent();
//        if (element1 instanceof PsiMember) {
//          isMember = true;
//          break;
//        }
//      }
//      if (isMember && !(element1 instanceof PsiAnonymousClass || element1.getParent() instanceof PsiAnonymousClass)) {
//        boolean drawSeparator = false;
//        int category = getCategory(element1);
//        for (PsiElement child = element1.getPrevSibling(); child != null; child = child.getPrevSibling()) {
//          int category1 = getCategory(child);
//          if (category1 == 0) continue;
//          drawSeparator = category != 1 || category1 != 1;
//          break;
//        }
//
//        if (drawSeparator) {
//          GrDocComment comment = null;
//          if (element1 instanceof GrDocCommentOwner) {
//            comment = ((GrDocCommentOwner)element1).getDocComment();
//          }
//          LineMarkerInfo info =
//            new LineMarkerInfo<PsiElement>(element, comment != null ? comment.getTextRange() : element.getTextRange(), null,
//                                           Pass.UPDATE_ALL, NullableFunction.NULL, null,
//                                           GutterIconRenderer.Alignment.RIGHT);
//          EditorColorsScheme scheme = myColorsManager.getGlobalScheme();
//          info.separatorColor = scheme.getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
//          info.separatorPlacement = SeparatorPlacement.TOP;
//          return info;
//        }
//      }
      return null;
    }

  @Override
  public void collectSlowLineMarkers(final List<PsiElement> elements, final Collection<LineMarkerInfo> result) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    if (elements.isEmpty() || DumbService.getInstance(elements.get(0).getProject()).isDumb()) {
      return;
    }

  }
}
