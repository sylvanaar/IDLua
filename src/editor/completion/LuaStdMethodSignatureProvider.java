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

package com.sylvanaar.idea.Lua.editor.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.util.ProcessingContext;
import com.sylvanaar.idea.Lua.lang.documentor.MethodSignatureBundle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/29/11
 * Time: 7:10 AM
 */
public class LuaStdMethodSignatureProvider extends CompletionProvider<CompletionParameters> {

    static final List<LookupElement> methods;

    static {
        Set<String> keys = MethodSignatureBundle.getAllKeys();
        methods = new ArrayList<LookupElement>(keys.size());
        for(String k : keys)
            methods.add(new LuaLookupElement(k));
    }


    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(com.sylvanaar.idea.Lua.editor.completion.LuaStdMethodSignatureProvider.methods);
    }
}
