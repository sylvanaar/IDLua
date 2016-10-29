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

package com.sylvanaar.idea.Lua.debugger;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightVirtualFile;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiFileImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 5/15/11
 * Time: 3:04 AM
 */
public class LuaCodeFragment extends LuaPsiFileImpl {
    public LuaCodeFragment(Project project,
                               IElementType contentElementType,
                               boolean isPhysical,
                               @NonNls String name,
                               CharSequence text,
                               @Nullable PsiElement context) {
        super(TokenType.CODE_FRAGMENT,
                contentElementType,
                PsiManagerEx.getInstanceEx(project).getFileManager().createFileViewProvider(
                        new LightVirtualFile(name, FileTypeManager.getInstance().getFileTypeByFileName(name), text), isPhysical)
        );
        setContext(context);
        ((SingleRootFileViewProvider)getViewProvider()).forceCachedPsi(this);
    }
}
