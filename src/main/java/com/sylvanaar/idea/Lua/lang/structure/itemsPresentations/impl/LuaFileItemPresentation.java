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
package com.sylvanaar.idea.Lua.lang.structure.itemsPresentations.impl;

import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.structure.LuaElementPresentation;
import com.sylvanaar.idea.Lua.lang.structure.itemsPresentations.LuaItemPresentation;


 public class LuaFileItemPresentation extends LuaItemPresentation {
  public LuaFileItemPresentation(LuaPsiFile myElement) {
    super(myElement);
  }

  public String getPresentableText() {
    return LuaElementPresentation.getFilePresentableText(((LuaPsiFile) myElement));
  }
}
