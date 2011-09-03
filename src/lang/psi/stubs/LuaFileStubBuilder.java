/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.stubs;

import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;


public class LuaFileStubBuilder extends DefaultStubBuilder
{
  protected StubElement createStubForFile(PsiFile file)
  {
    if (file instanceof LuaPsiFile) {
      //System.out.println("File stub: " + file.getName());
      return new LuaFileStub((LuaPsiFile)file);
    }
    return super.createStubForFile(file);
  }
}