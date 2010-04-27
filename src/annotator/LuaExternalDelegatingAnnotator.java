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

package com.sylvanaar.idea.Lua.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.options.LuaOption;
import com.sylvanaar.idea.Lua.options.LuaOptions;
import org.jetbrains.annotations.NotNull;

import static com.sylvanaar.idea.Lua.options.LuaOptionNames.*;


/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 15, 2010
 * Time: 8:14:20 PM
 */
public class LuaExternalDelegatingAnnotator implements ExternalAnnotator {
    
    private static final LuaAnnotatorOption OPTION = new LuaAnnotatorOption();

    static {
        LuaOptions.getInstance().registerOption(SYNTAX_CHECK_TYPE, OPTION);
        LuaOptions.getInstance().loadValue(SYNTAX_CHECK_TYPE, SYNTAX_CHECK_TYPE_LUAJ);
    }


    static final ExternalAnnotator NULL_ANNOTATOR = new ExternalAnnotator() {
        @Override
        public void annotate(PsiFile psiFile, AnnotationHolder annotationHolder) {
        }
    };

    @NotNull
    ExternalAnnotator currentAnnotator = NULL_ANNOTATOR;

    public LuaExternalDelegatingAnnotator() {
        OPTION.setInstance(this);
    }

    public enum AnnotatorType {
        NONE, LUAC, LUAJ
    }

    @Override
    public void annotate(PsiFile psiFile, AnnotationHolder annotationHolder) {
        currentAnnotator.annotate(psiFile, annotationHolder);
    }



    public void setAnnotator(AnnotatorType annotator) {
       

        switch (annotator) {
            case NONE:
                currentAnnotator = NULL_ANNOTATOR;
                break;
            case LUAJ:
                currentAnnotator = LuaJExternalAnnotator.getInstance();
                break;
            case LUAC:
                currentAnnotator = LuaJExternalAnnotator.getInstance();
                break;
        }


        
        VirtualFileManager.getInstance().refresh(true);
    }



    public static class LuaAnnotatorOption implements LuaOption {
        LuaExternalDelegatingAnnotator instance = null;

        public void setAnnotator(AnnotatorType annotator) {
            this.annotator = annotator;
        }

        public LuaExternalDelegatingAnnotator getInstance() {
            return instance;
        }

        public AnnotatorType getAnnotator() {
            return annotator;
        }

        AnnotatorType annotator = AnnotatorType.NONE;

        void setInstance(LuaExternalDelegatingAnnotator instance) {
            this.instance = instance;
        }

        @Override
        public String getValue() {            
            switch (annotator) {
                case NONE:
                    return SYNTAX_CHECK_TYPE_NONE;
                case LUAJ:
                    return SYNTAX_CHECK_TYPE_LUAJ;
                case LUAC:
                    return SYNTAX_CHECK_TYPE_LUAC;
            }

            return SYNTAX_CHECK_TYPE_NONE;
        }
        
        public void setAnnotator(String annotator) {
            if (annotator.equalsIgnoreCase(SYNTAX_CHECK_TYPE_NONE))
                setAnnotator(AnnotatorType.NONE);
            if (annotator.equalsIgnoreCase(SYNTAX_CHECK_TYPE_LUAJ))
                setAnnotator(AnnotatorType.LUAJ);
            if (annotator.equalsIgnoreCase(SYNTAX_CHECK_TYPE_LUAC))
                setAnnotator(AnnotatorType.LUAC);
        }
        
        @Override
        public void setValue(String value) {

           setAnnotator(value);
            
            if (instance != null)
                instance.setAnnotator(getAnnotator());
        }
    }
}
