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

package com.sylvanaar.idea.Lua.editor.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.editor.highlighter.LuaHighlightingData;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 8, 2010
 * Time: 5:45:21 PM
 */
public class LuaAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

        if (element instanceof LuaParameter) {
            annotateParamter((LuaParameter) element, holder);
            return;
        }

        if (element instanceof LuaReferenceExpression) {
            annotateReference((LuaReferenceExpression) element, holder);
            return;
        }
        if (element instanceof LuaIdentifier) {
            annotateIdentifier((LuaIdentifier) element, holder);
            return;
        }
        
        if (element instanceof LuaReturnStatement) {
            LuaReturnStatement r = (LuaReturnStatement) element;

            if (r.isTailCall()) {
                final Annotation a = holder.createInfoAnnotation(r, null);
                a.setTextAttributes(LuaHighlightingData.TAIL_CALL);
            }
        }

    }

    private void annotateReference(LuaReferenceExpression ref, AnnotationHolder holder) {
        if (ref.resolve() instanceof LuaParameter) {
            holder.createInfoAnnotation(ref, null).setTextAttributes(LuaHighlightingData.PARAMETER); 
        }
    }

    private void annotateParamter(LuaParameter id, AnnotationHolder holder) {
        holder.createInfoAnnotation(id, null).setTextAttributes(LuaHighlightingData.PARAMETER);        
    }

    private void annotateIdentifier( LuaIdentifier id, AnnotationHolder holder) {
        TextAttributesKey attributesKey = null;

        if (id.isGlobal()) {
            attributesKey = LuaHighlightingData.GLOBAL_VAR;
        } else if (id.isLocal() && !id.getText().equals("...")) {
            attributesKey = LuaHighlightingData.LOCAL_VAR;
        }


        if (attributesKey != null) {
            final Annotation annotation = holder.createInfoAnnotation(id, null);
            annotation.setTextAttributes(attributesKey);
        }
    }
}
