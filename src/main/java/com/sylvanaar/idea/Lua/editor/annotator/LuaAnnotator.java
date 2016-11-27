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

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.sylvanaar.idea.Lua.editor.highlighter.LuaHighlightingData;
import com.sylvanaar.idea.Lua.lang.luadoc.highlighter.LuaDocSyntaxHighlighter;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocReferenceElement;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocTag;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaLiteralExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaStringLiteralExpressionImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaCompoundReferenceElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.impl.symbols.LuaGlobalDeclarationImpl;
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
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof LuaPsiElement) {
            myHolder = holder;
            ((LuaPsiElement) element).accept(this);
            myHolder = null;
        }
    }

    public void visitReturnStatement(LuaReturnStatement stat) {
        super.visitReturnStatement(stat);

        if (stat.isTailCall()) {
            final Annotation a = myHolder.createInfoAnnotation(stat, null);
            a.setTextAttributes(LuaHighlightingData.TAIL_CALL);
        }
    }

    @Override
    public void visitLiteralExpression(LuaLiteralExpression e) {
        super.visitLiteralExpression(e);

        if (e instanceof LuaStringLiteralExpressionImpl) {
            if (!((LuaStringLiteralExpressionImpl) e).isClosed()) {
                myHolder.createErrorAnnotation(e, "Unterminated String Constant");
            }
        }
    }

    @Override
    public void visitDocReference(LuaDocReferenceElement ref) {
        super.visitDocReference(ref);

        PsiElement e = ref.resolve();

        highlightReference(ref, e);
    }

    @Override
    public void visitDocTag(LuaDocTag e) {
        super.visitDocTag(e);

        LuaDocSyntaxHighlighter highlighter = new LuaDocSyntaxHighlighter();

        PsiElement element = e.getFirstChild();
        while (element != null) {
            if (element instanceof ASTNode) {
                ASTNode astNode = (ASTNode) element;
                TextAttributesKey[] keys = highlighter.getTokenHighlights(astNode.getElementType());
                for (TextAttributesKey key : keys) {
                    final Annotation a = myHolder.createInfoAnnotation(element, null);
                    a.setTextAttributes(key);
                }
            }
            element = element.getNextSibling();
        }
    }

    @Override
    public void visitCompoundReference(LuaCompoundReferenceElementImpl ref) {
    }

    public void visitReferenceElement(LuaReferenceElement ref) {
        super.visitReferenceElement(ref);
        LuaSymbol e;

        e = (LuaSymbol) ref.resolve();

        if (e != null) {
            highlightReference(ref, e);
        }
    }


    private void highlightReference(PsiReference ref, PsiElement e) {
        if (e instanceof LuaParameter) {
            final Annotation a = myHolder.createInfoAnnotation((PsiElement)ref, null);
            a.setTextAttributes(LuaHighlightingData.PARAMETER);
        }
//        else if (e instanceof LuaIdentifier) {
//            LuaIdentifier id = (LuaIdentifier) e;
//            TextAttributesKey attributesKey = null;
//
//            if (id instanceof LuaGlobal) {
//                attributesKey = LuaHighlightingData.GLOBAL_VAR;
//            } else if (id instanceof LuaLocal && !id.getText().equals("...")) {
//                attributesKey = LuaHighlightingData.LOCAL_VAR;
//            } else if (id instanceof LuaFieldIdentifier) {
//                attributesKey = LuaHighlightingData.FIELD;
//            }
//
//            if (attributesKey != null) {
//                final Annotation annotation = myHolder.createInfoAnnotation(ref.getElement(), null);
//                annotation.setTextAttributes(attributesKey);
//            }
//            if (ref.getElement() instanceof LuaUpvalueIdentifier) {
//                final Annotation a = myHolder.createInfoAnnotation((PsiElement) ref, null);
//                a.setTextAttributes(LuaHighlightingData.UPVAL);
//            }
//        }
    }

//    @Override
//    public void visitKeyValueInitializer(LuaKeyValueInitializer e) {
//        super.visitKeyValueInitializer(e);
//         e.getFieldKey().setLuaType(e.getFieldValue().getLuaType());
//    }

//    @Override
//    public void visitDeclarationStatement(LuaDeclarationStatement e) {
//        super.visitDeclarationStatement(e);
//
//        if (e instanceof LuaLocalDefinitionStatement) {
//            LuaIdentifierList left = ((LuaLocalDefinitionStatement) e).getLeftExprs();
//            LuaExpressionList right = ((LuaLocalDefinitionStatement) e).getRightExprs();
//
//            if (right == null || right.count() == 0) return;
//
//            boolean allNil = true;
//            for (LuaExpression expr : right.getLuaExpressions())
//                if (!expr.getText().equals("nil")) {
//                    allNil = false;
//                    break;
//                }
//
//            if (allNil) {
//                int assignment = ((LuaLocalDefinitionStatement) e).getOperatorElement().getTextOffset();
//
//                myHolder.createWeakWarningAnnotation(new TextRange(assignment, right.getTextRange().getEndOffset()),
//                                null);
//            }
//        }
//    }




    public void visitDeclarationExpression(LuaDeclarationExpression dec) {
        if (!(dec.getContext() instanceof LuaParameter)) {
            final Annotation a = myHolder.createInfoAnnotation(dec, null);

            if (dec instanceof LuaLocalDeclarationImpl) a.setTextAttributes(LuaHighlightingData.LOCAL_VAR);
            else if (dec instanceof LuaGlobalDeclarationImpl) a.setTextAttributes(LuaHighlightingData.GLOBAL_VAR);
        }
    }

    public void visitParameter(LuaParameter id) {
        if (id.getTextLength() == 0) return;

        addSemanticHighlight(id, LuaHighlightingData.PARAMETER);
    }

    public void visitIdentifier(LuaIdentifier id) {
        if ((id != null) && id instanceof LuaGlobal) {
            addSemanticHighlight(id, LuaHighlightingData.GLOBAL_VAR);
            return;
        }
        if (id instanceof LuaFieldIdentifier) {
            addSemanticHighlight(id, LuaHighlightingData.FIELD);
            return;
        }
        if (id instanceof LuaUpvalueIdentifier) {
            addSemanticHighlight(id, LuaHighlightingData.UPVAL);
        } else if (id instanceof LuaLocalIdentifier) {
            PsiReference reference = id.getReference();
            if (reference != null) {
                PsiElement resolved = reference.resolve();
                if (resolved instanceof LuaParameter)
                    return;
            }
            final Annotation annotation = myHolder.createInfoAnnotation(id, null);
            annotation.setTextAttributes(LuaHighlightingData.LOCAL_VAR);
        }

    }

    private void addSemanticHighlight(LuaIdentifier id, TextAttributesKey key) {
        myHolder.createInfoAnnotation(id, null).setTextAttributes(key);
    }
}
