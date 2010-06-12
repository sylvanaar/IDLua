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

package com.sylvanaar.idea.Lua.lang.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;

import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.structure.impl.LuaFileStructureViewElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 3:25:48 PM
 */
public class LuaStructureViewModel  extends TextEditorBasedStructureViewModel {
  private final LuaPsiFile myRootElement;

  private static final Class[] SUITABLE_CLASSES =
    new Class[]{LuaPsiFile.class, LuaFunctionDefinitionStatement.class};

  public LuaStructureViewModel(LuaPsiFile rootElement) {
    super(rootElement);
    myRootElement = rootElement;
  }

  protected PsiFile getPsiFile() {
    return myRootElement;
  }

  @NotNull
  public StructureViewTreeElement getRoot() {
    return new LuaFileStructureViewElement(myRootElement);
  }

  @NotNull
  public Sorter[] getSorters() {
    return new Sorter[]{Sorter.ALPHA_SORTER};
  }

  @NotNull
  protected Class[] getSuitableClasses() {
    return SUITABLE_CLASSES;
  }

//  protected Object findAcceptableElement(PsiElement element) {
//    while (element != null && !(element instanceof PsiDirectory)) {
//      if (isSuitable(element)) {
//
//        return element;
//      }
//      element = element.getParent();
//    }
//    return null;
//  }

//  @Override
//  protected boolean isSuitable(final PsiElement element) {
//    if (super.isSuitable(element)) {
//      if (element instanceof LuaFunction) {
//        return true;
//      }
//    }
//    return false;
//  }
}