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

package com.sylvanaar.idea.Lua;

import com.intellij.lang.Language;

/**
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Jan 27, 2005
 * Time: 6:03:49 PM
 */
public class LuaLanguage extends Language {

    public LuaLanguage() {
        super("Lua");
    }

//    public FoldingBuilder getFoldingBuilder() {
//        return new LuaFoldingBuilder();
//    }
//
//    public PairedBraceMatcher getPairedBraceMatcher() {
//        return new JSBraceMatcher();
//    }
//
//    public Annotator getAnnotator() {
//        return ANNOTATOR;
//    }

//    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
//        return new TreeBasedStructureViewBuilder() {
//            public StructureViewModel createStructureViewModel() {
//                return new JSStructureViewModel((JSElement) psiFile);
//            }
//        };
//    }
//
//    @NotNull
//    public FindUsagesProvider getFindUsagesProvider() {
//        return new LuaFindUsagesProvider();
//    }
//
//    public Commenter getCommenter() {
//        return new LuaCommenter();
//    }
//
//    public FormattingModelBuilder getFormattingModelBuilder() {
//        return new FormattingModelBuilder() {
//            @NotNull
//            public FormattingModel createModel(final PsiElement element, final CodeStyleSettings settings) {
//                return new JSFormattingModel(element.getContainingFile(), settings, new JSBlock(element.getNode(), null, null, null, settings));
//            }
//        };
//    }
//
//    @NotNull
//    public SurroundDescriptor[] getSurroundDescriptors() {
//        return SURROUND_DESCRIPTORS;
//    }
}
  
