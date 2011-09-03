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

package com.sylvanaar.idea.Lua.editor.completion;

import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import org.jetbrains.annotations.NotNull;


/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 9/3/11
 * Time: 7:41 AM
 */
public class LuaCompletionWeigher extends CompletionWeigher {
    private static final Logger log = Logger.getInstance("Lua.CompletionWeigher");
    @Override
    public Comparable weigh(@NotNull LookupElement element, @NotNull CompletionLocation location) {
        Object o = element.getObject();
        if (o instanceof ResolveResult) {
          o = ((ResolveResult)o).getElement();
        }

        if (o instanceof String) return SymbolWeight.anyGlobalFromCache;

        final PsiElement position = location.getCompletionParameters().getPosition();
        final String text = position.getText();
        final PsiFile containingFile = position.getContainingFile();
        if (!(containingFile instanceof LuaPsiFile )) {
          return null;
        }

        if (! (o instanceof LuaSymbol)) return null;

        if (position instanceof LuaCompoundIdentifier || StringUtil.containsAnyChar(".:[]", text)) {

        } else {
            if (o instanceof LuaCompoundIdentifier) {
                return null; // for now TODO!!
            }
            if (o instanceof LuaLocalIdentifier) return SymbolWeight.aLocal;

            if (!((LuaSymbol) o).isValid()) return null;
            final PsiFile completionFile = ((LuaSymbol) o).getContainingFile();
            if (containingFile.equals(completionFile)) return SymbolWeight.aGlobalInFile;

            final VirtualFile completionFileVirtualFile = completionFile.getVirtualFile();
            final VirtualFile containingFileVirutalFile = location.getCompletionParameters().getOriginalFile().getVirtualFile();

            if (completionFileVirtualFile == null) return null;
            if (containingFileVirutalFile == null) return null;

            ProjectFileIndex index = ProjectRootManager.getInstance(location.getProject()).getFileIndex();

            if (index.isInContent(completionFileVirtualFile))
                return SymbolWeight.aProjectGlobal;

            

            if ((index.isInLibraryClasses(completionFileVirtualFile)))
                return SymbolWeight.aLibraryGlobal;
        }


        return null;
    }

  static enum SymbolWeight {
    anOnlyReadGlobal, anyGlobalFromCache, aLibraryGlobal, anSDKGlobal, aProjectGlobal, aGlobalInFile, aLocal
  }

  static enum CompoundSymbolWeight {
      anOnlyReadGlobal, anSDKGlobal, aLibraryGlobal, aProjectGlobal, aGlobalInFile
  }
}
