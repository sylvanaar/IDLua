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
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 3:29:14 AM
 */
public interface DocumentationSource {
        /**
         * Returns the documentation for the given element. If there is no
         * documentation available null is returned.
         *
         * @param element         The element for which the documentation should be provided.
         * @param originalElement
         * @return The documentation or null if the PsiElement is not supported by this source.
         */
        @Nullable
        String documentation(PsiElement element, PsiElement originalElement);

        /**
         * Return the URL which provides further information for the given PSI element and the given
         * original element.
         *
         * @param element         The PSI element for which the documentation URL should be provided.
         * @param originalElement
         * @return A String which is an URL to an external source with more information about the command.
         */
        @Nullable
        String documentationUrl(PsiElement element, PsiElement originalElement);
    }
