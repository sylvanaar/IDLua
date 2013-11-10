/*
 * Copyright 2013 Jon S Akhtar (Sylvanaar)
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
package com.sylvanaar.idea.Lua.editor.completion.smartEnter;

import com.intellij.lang.SmartEnterProcessorWithFixers;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.editor.completion.smartEnter.processors.LuaPlainEnterProcessor;
import org.jetbrains.annotations.NotNull;

public class LuaSmartEnterProcessor extends SmartEnterProcessorWithFixers {
    public static final PsiElement[] NO_ELEMENTS = new PsiElement[0];
    private static final Logger log = Logger.getInstance(LuaSmartEnterProcessor.class);

    public LuaSmartEnterProcessor() {
        addFixers(
                new Fixer() {
                    @Override
                    public void apply(@NotNull Editor editor, @NotNull SmartEnterProcessorWithFixers processor,
                                      @NotNull PsiElement element) throws IncorrectOperationException {

                    }
                });

        addEnterProcessors(new LuaPlainEnterProcessor());
    }
}
