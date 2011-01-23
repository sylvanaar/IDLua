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

package com.sylvanaar.idea.Lua.lang.psi.resolve.processors;

import com.intellij.psi.scope.ElementClassHint;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.containers.HashSet;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResult;


public abstract class ResolveProcessor implements PsiScopeProcessor, NameHint, ElementClassHint
{
  protected HashSet<LuaResolveResult> myCandidates = new HashSet<LuaResolveResult>();
  protected final String myName;

  public ResolveProcessor(String myName)
  {
    this.myName = myName;
  }

  public LuaResolveResult[] getCandidates()
  {
    return myCandidates.toArray(new LuaResolveResult[myCandidates.size()]);
  }

  public <T> T getHint(Class<T> hintClass)
  {
    if (NameHint.class == hintClass && myName != null)
    {
      return (T) this;
    }
    else if (ElementClassHint.class == hintClass)
    {
      return (T) this;
    }

    return null;
  }

  public void handleEvent(Event event, Object o)
  {
  }
}
