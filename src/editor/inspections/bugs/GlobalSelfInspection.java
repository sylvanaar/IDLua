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

package com.sylvanaar.idea.Lua.editor.inspections.bugs;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.editor.inspections.AbstractInspection;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobal;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 7:33:20 AM
 */
public class GlobalSelfInspection extends AbstractInspection {
    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
       return "Usage of global self";
    }

    @Override
    public String getStaticDescription() {
        return "Looks for usage of self as a global. This usually indicates a missing ':' in the function definition.";
    }

    @NotNull
    @Override
    public String getGroupDisplayName() {
        return PROBABLE_BUGS;
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new LuaElementVisitor() {
            public void visitReferenceElement(LuaReferenceElement var) {
                super.visitReferenceElement(var);
                PsiElement e = var.getElement();
                if (e instanceof LuaGlobal && e.getText().equals("self"))
                    holder.registerProblem(e, "Usage of global self", LocalQuickFix.EMPTY_ARRAY);
            }
        };
    }
    

}
