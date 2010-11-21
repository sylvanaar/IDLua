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


// Customized version of IdentifierHilighter plugin with many features removed
package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Query;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class LuaIdentifierHighlighterEditorComponent implements CaretListener, DocumentListener {
    static Logger log = Logger.getInstance("#LuaIdentifierHighlighterEditorComponent");

    protected LuaIdentifierHighlighterAppComponent _appComponent = null;
    protected Editor _editor = null;
    protected ArrayList<RangeHighlighter> _highlights = null;
    protected ArrayList<Boolean> _forWriting = null;
    protected String _currentIdentifier = null;

    protected int _startElem = -1;
    protected int _currElem = -1;
    protected int _declareElem = -1;

    protected boolean _identifiersLocked = false;
    protected PsiReferenceComparator _psiRefComp = null;

    public LuaIdentifierHighlighterEditorComponent(LuaIdentifierHighlighterAppComponent appComponent, Editor editor) {
        _appComponent = appComponent;

        _editor = editor;
        _editor.getCaretModel().addCaretListener(this);
        _editor.getDocument().addDocumentListener(this);
        _psiRefComp = new PsiReferenceComparator();
    }

    //CaretListener interface implementation
    public void caretPositionChanged(final CaretEvent ce) {
        //Execute later so we are not searching Psi model while updating it
        //Fixes Idea 7 exception
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                handleCaretPositionChanged(ce);
            }
        });
    }

    protected void handleCaretPositionChanged(CaretEvent ce) {
        if (_editor == null)
            return;
        if (_editor.getProject() == null)
            return;
        if (_editor.getDocument() == null)
            return;

        PsiDocumentManager pdm = PsiDocumentManager.getInstance(_editor.getProject());
        PsiFile pFile = pdm.getPsiFile(_editor.getDocument());
        if (pFile == null)
            return;

        PsiElement pElem = null;


        if (pdm.isUncommited(_editor.getDocument())) {
            // TODO: Do we really want to commit the document every time we move the cursor?
            //    pdm.commitDocument(_editor.getDocument());
            pElem = null;
        } else {
            pElem = pFile.findElementAt(_editor.getCaretModel().getOffset());
        }

        if (pElem == null || pElem.getParent() == null || !(pElem.getParent() instanceof LuaIdentifier) || pElem.getText().equals("."))
            pElem = null;

        if (pElem == null) {
            if (_highlights != null)
                clearState();
            return;
        }

        //We have a pElem
        //Check if different identifier than before
        if (_highlights != null) {
            int foundElem = -1;
            TextRange pElemRange = pElem.getTextRange();
            for (int i = 0; i < _highlights.size(); i++) {
                RangeHighlighter highlight = _highlights.get(i);
                if ((highlight.getStartOffset() == pElemRange.getStartOffset()) && (highlight.getEndOffset() == pElemRange.getEndOffset())) {
                    foundElem = i;
                    break;
                }
            }
            if (foundElem != -1) {
                if (foundElem != _currElem) {
                    moveIdentifier(foundElem);
                    _startElem = foundElem;
                }
                return;
            } else
                clearState();
        }
        _currentIdentifier = pElem.getText();
        log.info("Caret on identifier " + pElem.getText());
        ArrayList<PsiElement> elems = new ArrayList<PsiElement>();
        PsiReference pRef = pFile.findReferenceAt(_editor.getCaretModel().getOffset());
        if (pRef == null) {
            //See if I am a declaration so search for references to me
            PsiElement pElemCtx = pElem.getContext();

            if (pElemCtx == LuaElementTypes.VARIABLE)
                log.info("Caret on VARIABLE:" + pElem);
            else if (pElemCtx == LuaElementTypes.PARAMETER)
                log.info("Caret on PARAMETER:" + pElem);
            Query<PsiReference> q = ReferencesSearch.search(pElemCtx, GlobalSearchScope.fileScope(pFile));
            PsiReference qRefs[] = q.toArray(new PsiReference[0]);

            //Sort by text offset
            Arrays.sort(qRefs, _psiRefComp);
            for (PsiReference qRef : qRefs) {
                //Find child PsiIdentifier so highlight is just on it
                PsiElement qRefElem = qRef.getElement();
                LuaIdentifier qRefElemIdent = findChildIdentifier(qRefElem, pElem.getText());
                if (qRefElemIdent == null)
                    continue;
                //Skip elements from other files
                if (!areSameFiles(pFile, qRefElemIdent.getContainingFile()))
                    continue;
                //Check if I should be put in list first to keep it sorted by text offset
                if ((_declareElem == -1) && (pElem.getTextOffset() <= qRefElemIdent.getTextOffset())) {
                    elems.add(pElem);
                    _declareElem = elems.size() - 1;
                }
                elems.add(qRefElemIdent);
            }
            //If haven't put me in list yet, put me in last
            if (_declareElem == -1) {
                elems.add(pElem);
                _declareElem = elems.size() - 1;
            }
        } else {
            //Resolve to declaration
            log.info("resolving " + pRef);
            PsiElement pRefElem;
            try {
                pRefElem = pRef.resolve();
            } catch (Throwable t) {
                pRefElem = null;
            }
            if (pRefElem != null) {
                if (pRefElem == LuaElementTypes.VARIABLE)
                    log.info("Resolved to VARIABLE:" + pElem);
                else if (pRefElem == LuaElementTypes.PARAMETER)
                    log.info("Resolved to PARAMETER:" + pElem);
                else if (pRefElem == LuaElementTypes.LOCAL_NAME_DECL)
                    log.info("Resolved to LOCAL_NAME_DECL:" + pElem);
            }
            if (pRefElem != null) {
                LuaIdentifier pRefElemIdent = findChildIdentifier(pRefElem, pElem.getText());
                if (pRefElemIdent != null) {
                    //Search for references to my declaration
                    Query<PsiReference> q = ReferencesSearch.search(pRefElemIdent, GlobalSearchScope.fileScope(pFile));
                    PsiReference qRefs[] = q.toArray(new PsiReference[0]);
                    //Sort by text offset
                    Arrays.sort(qRefs, _psiRefComp);
                    for (PsiReference qRef : qRefs) {
                        //Find child PsiIdentifier so highlight is just on it
                        PsiElement qRefElem = qRef.getElement();
                        LuaIdentifier qRefElemIdent = findChildIdentifier(qRefElem, pElem.getText());
                        if (qRefElemIdent == null)
                            continue;
                        //Skip elements from other files
                        if (!areSameFiles(pFile, qRefElemIdent.getContainingFile()))
                            continue;
                        //Check if I should be put in list first to keep it sorted by text offset
                        if ((areSameFiles(pFile, pRefElemIdent.getContainingFile())) && (_declareElem == -1) && (pRefElemIdent.getTextOffset() <= qRefElemIdent.getTextOffset())) {
                            elems.add(pRefElemIdent);
                            _declareElem = elems.size() - 1;
                        }
                        elems.add(qRefElemIdent);
                    }
                    if (elems.size() == 0) {
                        //Should at least put the original found element at cursor in list
                        //Check if I should be put in list first to keep it sorted by text offset
                        if ((areSameFiles(pFile, pRefElemIdent.getContainingFile())) && (_declareElem == -1) && (pRefElemIdent.getTextOffset() <= pElem.getTextOffset())) {
                            elems.add(pRefElemIdent);
                            _declareElem = elems.size() - 1;
                        }
                        elems.add(pElem);
                    }
                    //If haven't put me in list yet, put me in last
                    if ((areSameFiles(pFile, pRefElemIdent.getContainingFile())) && (_declareElem == -1)) {
                        elems.add(pRefElemIdent);
                        _declareElem = elems.size() - 1;
                    }
                }
            } else {
                //No declaration found, so resort to simple string search
                PsiSearchHelper search = pElem.getManager().getSearchHelper();
                PsiElement idents[] = search.findCommentsContainingIdentifier(pElem.getText(), GlobalSearchScope.fileScope(pFile));
                for (PsiElement ident : idents)
                    elems.add(ident);
            }
        }
        _highlights = new ArrayList<RangeHighlighter>();
        _forWriting = new ArrayList<Boolean>();
        for (int i = 0; i < elems.size(); i++) {
            PsiElement elem = elems.get(i);
            TextRange range = elem.getTextRange();
            //Verify range is valid against current length of document
            if ((range.getStartOffset() >= _editor.getDocument().getTextLength()) || (range.getEndOffset() >= _editor.getDocument().getTextLength()))
                continue;
            boolean forWriting = isForWriting(elem);
            _forWriting.add(forWriting);
            RangeHighlighter rh;
            if (elem.getTextRange().equals(pElem.getTextRange())) {
                _startElem = i;
                _currElem = i;
                rh = _editor.getMarkupModel().addRangeHighlighter(range.getStartOffset(), range.getEndOffset(), getHighlightLayer(), getActiveHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);

                rh.setErrorStripeMarkColor(getActiveHighlightColor(forWriting).getBackgroundColor());
            } else {
                rh = _editor.getMarkupModel().addRangeHighlighter(range.getStartOffset(), range.getEndOffset(), getHighlightLayer(), getHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);

                rh.setErrorStripeMarkColor(getHighlightColor(forWriting).getBackgroundColor());
            }

            rh.setErrorStripeTooltip(_currentIdentifier + " [" + i + "]");
            _highlights.add(rh);
        }
    }

    protected boolean isForWriting(PsiElement elem) {
        PsiExpression parentExpression = PsiTreeUtil.getParentOfType(elem, PsiExpression.class);
        if (parentExpression != null)
            return (PsiUtil.isAccessedForWriting(parentExpression));
        else {
            PsiVariable parentVariable = PsiTreeUtil.getParentOfType(elem, PsiVariable.class);
            if (parentVariable != null) {
                PsiExpression initializer = parentVariable.getInitializer();
                return (initializer != null);
            }
        }
        return (false);
    }

    protected boolean areSameFiles(PsiFile editorFile, PsiFile candidateFile) {
        if ((editorFile == null) && (candidateFile == null))
            return (true);
        if (editorFile == null)
            return (true);
        if (candidateFile == null)
            return (true);
        String editorFileName = editorFile.getName();
        String candidateFileName = candidateFile.getName();
        if ((editorFileName == null) && (candidateFileName == null))
            return (true);
        if (editorFileName == null)
            return (true);
        if (candidateFileName == null)
            return (true);
        return (editorFileName.equals(candidateFileName));
    }

    protected LuaIdentifier findChildIdentifier(PsiElement parent, String childText) {
        if ((parent instanceof LuaIdentifier) && (parent.getText().equals(childText)))
            return ((LuaIdentifier) parent);
        //Packages don't implement getChildren yet they don't throw an exception.  It is caught internally so I can't catch it.
        PsiElement children[] = parent.getChildren();
        if (children.length == 0)
            return (null);
        for (PsiElement child : children) {
            LuaIdentifier foundElem = findChildIdentifier(child, childText);
            if (foundElem != null)
                return (foundElem);
        }
        return (null);
    }

    protected boolean isHighlightEnabled() {
        return _appComponent.isEnabled();
    }


    protected TextAttributes getActiveHighlightColor(boolean forWriting) {
        TextAttributes retVal = new TextAttributes();
        if (!isHighlightEnabled())
            return (retVal);

        retVal.setBackgroundColor(Color.GREEN);
        return (retVal);
    }

    protected TextAttributes getHighlightColor(boolean forWriting) {
        TextAttributes retVal = new TextAttributes();
        if (!isHighlightEnabled())
            return (retVal);

        retVal.setBackgroundColor(Color.YELLOW);
        return (retVal);
    }

    protected int getHighlightLayer() {
        return (HighlighterLayer.ADDITIONAL_SYNTAX);
    }

    //DocumentListener interface implementation
    public void beforeDocumentChange(DocumentEvent de) {
    }

    public void documentChanged(DocumentEvent de) {
        caretPositionChanged(null);
    }

    protected void clearState() {
        if (_highlights != null) {
            for (RangeHighlighter highlight : _highlights)
                _editor.getMarkupModel().removeHighlighter(highlight);
        }
        _highlights = null;
        _forWriting = null;
        _currentIdentifier = null;
        _startElem = -1;
        _currElem = -1;
        _declareElem = -1;
    }

    public void dispose() {
        clearState();
        _editor.getCaretModel().removeCaretListener(this);
        _editor.getDocument().removeDocumentListener(this);
        _editor = null;
    }

    public void repaint() {
        if (_highlights == null)
            return;
        for (int i = 0; i < _highlights.size(); i++) {
            RangeHighlighter rh = _highlights.get(i);
            boolean forWriting = _forWriting.get(i);
            int startOffset = rh.getStartOffset();
            int endOffset = rh.getEndOffset();
            _editor.getMarkupModel().removeHighlighter(rh);
            if (i == _currElem) {
                rh = _editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset, getHighlightLayer(), getActiveHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);
                rh.setErrorStripeMarkColor(getActiveHighlightColor(forWriting).getBackgroundColor());
            } else {
                rh = _editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset, getHighlightLayer(), getHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);
                rh.setErrorStripeMarkColor(getHighlightColor(forWriting).getBackgroundColor());
            }
            rh.setErrorStripeTooltip(_currentIdentifier + " [" + i + "]");
            _highlights.set(i, rh);
        }
    }

    protected void moveIdentifier(int index) {
        try {
            if (_currElem != -1) {
                RangeHighlighter rh = _highlights.get(_currElem);
                boolean forWriting = _forWriting.get(_currElem);
                int startOffset = rh.getStartOffset();
                int endOffset = rh.getEndOffset();
                _editor.getMarkupModel().removeHighlighter(rh);
                rh = _editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset, getHighlightLayer(), getHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);
                rh.setErrorStripeMarkColor(getHighlightColor(forWriting).getBackgroundColor());
                rh.setErrorStripeTooltip(_currentIdentifier + " [" + _currElem + "]");
                _highlights.set(_currElem, rh);
            }
            _currElem = index;
            RangeHighlighter rh = _highlights.get(_currElem);
            boolean forWriting = _forWriting.get(_currElem);
            int startOffset = rh.getStartOffset();
            int endOffset = rh.getEndOffset();
            _editor.getMarkupModel().removeHighlighter(rh);
            rh = _editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset, getHighlightLayer(), getActiveHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);
            rh.setErrorStripeMarkColor(getActiveHighlightColor(forWriting).getBackgroundColor());
            rh.setErrorStripeTooltip(_currentIdentifier + " [" + _currElem + "]");
            _highlights.set(_currElem, rh);
        } catch (Throwable t) {
            //Ignore
        }
    }

    protected class PsiReferenceComparator implements Comparator<PsiReference> {
        public int compare(PsiReference ref1, PsiReference ref2) {
            int offset1 = ref1.getElement().getTextOffset();
            int offset2 = ref2.getElement().getTextOffset();
            return (offset1 - offset2);
        }
    }
}
