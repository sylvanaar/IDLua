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
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaVariable;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaGlobalDeclarationImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaGlobalUsageImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaLocalDeclarationImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
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

    @Override
    public void visitCompoundReferenceExpression(LuaVariable e) {
        super.visitCompoundReferenceExpression(e);
            LuaIdentifier s = e.reduceToIdentifier();

        if (s !=null)
            if (s instanceof LuaDeclarationExpression)
                visitDeclarationExpression((LuaDeclarationExpression) s);
            else
                visitReferenceElement((LuaReferenceElement) s);

        e.resolve();
    }

    public void visitReferenceElement(LuaReferenceElement ref) {
        PsiElement e = ref.resolve();

//        ResolveResult[] r = ref.multiResolve(false);
//
//        if (e==null && r.length>0)
//            e = r[0].getElement();
        
        if (e instanceof LuaParameter) {
            final Annotation a = myHolder.createInfoAnnotation(ref, null);
            a.setTextAttributes(LuaHighlightingData.PARAMETER);
        }
        else if (e instanceof LuaIdentifier) {
            LuaIdentifier id = (LuaIdentifier) e;
            TextAttributesKey attributesKey = null;

            if (id instanceof LuaGlobalIdentifier) {
                attributesKey = LuaHighlightingData.GLOBAL_VAR;
            } else if (id instanceof LuaLocalIdentifier &&
                    !(ref instanceof LuaUpvalueIdentifier) &&
                    !id.getText().equals("...")) {
                attributesKey = LuaHighlightingData.LOCAL_VAR;
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

            if (dec instanceof LuaLocalDeclarationImpl)
                a.setTextAttributes(LuaHighlightingData.LOCAL_VAR);
            else if (dec instanceof LuaGlobalDeclarationImpl)
                a.setTextAttributes(LuaHighlightingData.GLOBAL_VAR);
        }
    }

    public void visitParameter(LuaParameter id) {
        final Annotation a = myHolder.createInfoAnnotation(id, null);
        a.setTextAttributes(LuaHighlightingData.PARAMETER);
    }

    public void visitIdentifier(LuaIdentifier id) {
        if ((id != null) && id instanceof LuaGlobalUsageImpl) {
            final Annotation annotation = myHolder.createInfoAnnotation(id, null);
            annotation.setTextAttributes(LuaHighlightingData.GLOBAL_VAR);
            return;
        }
        if (id instanceof LuaFieldIdentifier) {
            final Annotation annotation = myHolder.createInfoAnnotation(id, null);
            annotation.setTextAttributes(LuaHighlightingData.FIELD);
            return;
        }
        if (id instanceof LuaUpvalueIdentifier) {
            final Annotation annotation = myHolder.createInfoAnnotation(id, null);
            annotation.setTextAttributes(LuaHighlightingData.UPVAL);
            return;
        }
//        if (id instanceof LuaLocalIdentifier) {
//            final Annotation annotation = myHolder.createInfoAnnotation(id, null);
//            annotation.setTextAttributes(LuaHighlightingData.LOCAL_VAR);
//            return;
//        }
    }
}
