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
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;

import org.jetbrains.annotations.NotNull;


/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 8, 2010
 * Time: 5:45:21 PM
 */
public class LuaAnnotator extends LuaElementVisitor implements Annotator {
    private AnnotationHolder myHolder = null;

    @Override
    public void annotate(@NotNull
    PsiElement element, @NotNull
    AnnotationHolder holder) {
        if (element instanceof LuaPsiElement) {
            myHolder = holder;
            ((LuaPsiElement) element).accept(this);
            myHolder = null;
        }
    }

    public void visitReturnStatement(LuaReturnStatement stat) {
        if (stat.isTailCall()) {
            final Annotation a = myHolder.createInfoAnnotation(stat, null);
            a.setTextAttributes(LuaHighlightingData.TAIL_CALL);
        }
    }

    public void visitReferenceExpression(LuaReferenceExpression ref) {
        final PsiElement e = ref.resolve();

        if (e instanceof LuaParameter) {
            final Annotation a = myHolder.createInfoAnnotation(ref, null);
            a.setTextAttributes(LuaHighlightingData.PARAMETER);
        } else if (e instanceof LuaIdentifier ||
                e instanceof LuaDeclarationStatement) {
            LuaIdentifier id = (LuaIdentifier) e;
            TextAttributesKey attributesKey = null;

            if (id.isGlobal()) {
                attributesKey = LuaHighlightingData.GLOBAL_VAR;
            } else if (id.isLocal() && !id.getText().equals("...")) {
                attributesKey = LuaHighlightingData.LOCAL_VAR;
            }

            if (attributesKey != null) {
                final Annotation annotation = myHolder.createInfoAnnotation(ref,
                        null);
                annotation.setTextAttributes(attributesKey);
            }
        }

        if (e == null) {
            LuaIdentifier id = (ref.getNameElement() != null)
                ? (LuaIdentifier) ref.getNameElement().getPsi() : null;
            TextAttributesKey attributesKey = null;

            if ((id != null) && id.isGlobal()) {
                attributesKey = LuaHighlightingData.GLOBAL_VAR;
            }

            if (attributesKey != null) {
                final Annotation annotation = myHolder.createInfoAnnotation(ref,
                        null);
                annotation.setTextAttributes(attributesKey);
            }
        }
    }

    public void visitDeclarationExpression(LuaDeclarationExpression dec) {
        if (!(dec.getContext() instanceof LuaParameter)) {
            final Annotation a = myHolder.createInfoAnnotation(dec, null);
            a.setTextAttributes(LuaHighlightingData.LOCAL_VAR);
        }
    }

    public void visitParameter(LuaParameter id) {
        final Annotation a = myHolder.createInfoAnnotation(id, null);
        a.setTextAttributes(LuaHighlightingData.PARAMETER);
    }

    public void visitIdentifier(LuaIdentifier id) {
        //        TextAttributesKey attributesKey = null;
        //
        //        if (id.isGlobal()) {
        //            attributesKey = LuaHighlightingData.GLOBAL_VAR;
        //        } else if (id.isLocal() && !id.getText().equals("...")) {
        //            attributesKey = LuaHighlightingData.LOCAL_VAR;
        //        }
        //
        //
        //        if (attributesKey != null) {
        //            final Annotation annotation = myHolder.createInfoAnnotation(id, null);
        //            annotation.setTextAttributes(attributesKey);
        //        }
    }
}
