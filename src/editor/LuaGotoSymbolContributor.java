/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.editor;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.stubs.StubIndex;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 11/23/10
 * Time: 3:38 PM
 */
public class    LuaGotoSymbolContributor implements ChooseByNameContributor {
    @Override
    public String[] getNames(Project project, boolean b) {

        FileIndex fi = ProjectRootManager.getInstance(project).getFileIndex();

        final List<String> names = new ArrayList<String>();

      //  names.addAll(StubIndex.getInstance().getAllKeys(LuaGlobalDeclarationIndex.KEY, project));

        final Project myProject = project;

        fi.iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileOrDir) {
                if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
                    PsiFile file = PsiManager.getInstance(myProject).findFile(fileOrDir);

                    if (file instanceof LuaPsiFile) {
                        final LuaPsiFile lua = (LuaPsiFile) file;

                        for(LuaFunctionDefinitionStatement func: lua.getFunctionDefs()) {
                            names.add(func.getName());
                        }
                    }

                }
                return true;
            }
        });
        return names.toArray(new String[names.size()]);
    }

    @Override
    public NavigationItem[] getItemsByName(String s, String s1, Project project, boolean b) {
        FileIndex fi = ProjectRootManager.getInstance(project).getFileIndex();

        final List<NavigationItem> names = new ArrayList<NavigationItem>();

        final Project myProject = project;
        final String chosenName = s;

        fi.iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileOrDir) {
                if (fileOrDir.getFileType() == LuaFileType.LUA_FILE_TYPE) {
                    PsiFile file = PsiManager.getInstance(myProject).findFile(fileOrDir);

                    if (file instanceof LuaPsiFile) {
                        final LuaPsiFile lua = (LuaPsiFile) file;

                        for(LuaFunctionDefinitionStatement func: lua.getFunctionDefs()) {
                            if (func.getName().equals(chosenName))
                                names.add(new BaseNavigationItem(func, chosenName, null));
                        }
                    }

                }
                return true;
            }
        });
        return names.toArray(new NavigationItem[names.size()]);
    }

    /**
     * Wraps one entry to display in "Go To Symbol" dialog.
     */
    public static class BaseNavigationItem extends FakePsiElement {

      private final PsiElement myPsiElement;
      private final String myText;
      private final Icon myIcon;

      /**
       * Creates a new display item.
       *
       * @param psiElement The PsiElement to navigate to.
       * @param text       Text to show for this element.
       * @param icon       Icon to show for this element.
       */
      public BaseNavigationItem(@NotNull PsiElement psiElement, @NotNull @NonNls String text, @Nullable Icon icon) {
        myPsiElement = psiElement;
        myText = text;
        myIcon = icon;
      }

      public PsiElement getNavigationElement() {
        return myPsiElement;
      }

      public Icon getIcon(boolean flags) {
        return myIcon;
      }

      public ItemPresentation getPresentation() {
        return new ItemPresentation() {

          public String getPresentableText() {
            return myText;
          }

          @Nullable
          public String getLocationString() {
            return '(' + myPsiElement.getContainingFile().getName() + ')';
          }

          @Nullable
          public Icon getIcon(boolean open) {
            return myIcon;
          }

          @Nullable
          public TextAttributesKey getTextAttributesKey() {
            return null;
          }
        };
      }

      public PsiElement getParent() {
        return myPsiElement.getParent();
      }

      public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BaseNavigationItem that = (BaseNavigationItem)o;

        if (myPsiElement != null ? !myPsiElement.equals(that.myPsiElement) : that.myPsiElement != null) return false;
        if (myText != null ? !myText.equals(that.myText) : that.myText != null) return false;

        return true;
      }

      public int hashCode() {
        int result;
        result = (myPsiElement != null ? myPsiElement.hashCode() : 0);
        result = 31 * result + (myText != null ? myText.hashCode() : 0);
        return result;
      }
    }


}
