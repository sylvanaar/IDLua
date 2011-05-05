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

package com.sylvanaar.idea.Lua.lang.resolve;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.sylvanaar.idea.Lua.LightLuaTestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * @author ven
 */
public abstract class LuaResolveTestCase extends LightLuaTestCase {
  @NonNls protected static final String MARKER = "<ref>";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (new File(myFixture.getTestDataPath() + "/" + getTestName(true)).exists()) {
      myFixture.copyDirectoryToProject(getTestName(true), "");
    }
  }

  protected PsiReference configureByFile(@NonNls String filePath, @Nullable String newFilePath) {
    filePath = StringUtil.trimStart(filePath, getTestName(true) + "/");
    final VirtualFile vFile = myFixture.getTempDirFixture().getFile(filePath);
    assertNotNull("file " + filePath + " not found", vFile);

    String fileText;
    try {
      fileText = StringUtil.convertLineSeparators(VfsUtil.loadText(vFile));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    int offset = fileText.indexOf(MARKER);
    assertTrue(offset >= 0);
    fileText = fileText.substring(0, offset) + fileText.substring(offset + MARKER.length());

    if (newFilePath == null) {
      myFixture.configureByText("aaa." + vFile.getExtension(), fileText);
    }
    else {
      myFixture.configureByText(newFilePath, fileText);
    }

    PsiReference ref = myFixture.getFile().findReferenceAt(offset);
    assertNotNull(ref);
    return ref;
  }

  protected PsiReference configureByFile(@NonNls String filePath) {
    return configureByFile(filePath, null);
  }

  @Nullable
  protected PsiElement resolve(String fileName) {
    PsiReference ref = configureByFile(getTestName(true) + "/" + fileName);
    return ref.resolve();
  }

//  @Nullable
//  protected LuaResolveResult advancedResolve(String fileName) {
//    PsiReference ref = configureByFile(getTestName(true) + "/" + fileName);
//    assertInstanceOf(ref, LuaReferenceElement.class);
//    return ((LuaReferenceElement)ref).advancedResolve();
//  }

}
