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

package com.sylvanaar.idea.Lua.lang.documentor;


import com.intellij.psi.PsiElement;

/**
 * Provides documentation for internal commands.
 * <p/>
 * Date: 03.05.2009
 * Time: 18:25:29
 *
 * @author Joachim Ansorg
 */
class StandardFunctionDocumentation implements DocumentationSource {

    @Override
    public String documentation(PsiElement element, PsiElement originalElement) {
        return MethodSignatureBundle.message(element.getText());
    }

    @Override
    public String documentationUrl(PsiElement element, PsiElement originalElement) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
