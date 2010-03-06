package com.sylvanaar.idea.Lua

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Jan 27, 2005
 * Time: 6:03:49 PM
 * To change this template use File | Settings | File Templates.
 */

public class LuaLanguage extends Language {

    public LuaLanguage() {

        super("Lua");

        SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(this, new SingleLazyInstanceSyntaxHighlighterFactory() {
            @NotNull
            protected SyntaxHighlighter createHighlighter() {
                return new com.sylvanaar.idea.Lua.lexer.LuaSyntaxHighlighter();
            }
        });
    }

}
/*
public class LuaLanguage extends Language {
    private static final JSAnnotatingVisitor ANNOTATOR = new JSAnnotatingVisitor();
    private final static SurroundDescriptor[] SURROUND_DESCRIPTORS = new SurroundDescriptor[]{
            new JSExpressionSurroundDescriptor(),
            new JSStatementsSurroundDescriptor()
    };

    public LuaLanguage() {
        super("Lua", "text/Lua", "application/Lua");
    }

    public ParserDefinition getParserDefinition() {
        return new LuaParserDefinition();
    }

    @NotNull
    public SyntaxHighlighter getSyntaxHighlighter(Project project) {
        return new JSHighlighter();
    }

    public FoldingBuilder getFoldingBuilder() {
        return new LuaFoldingBuilder();
    }

    public PairedBraceMatcher getPairedBraceMatcher() {
        return new JSBraceMatcher();
    }

    public Annotator getAnnotator() {
        return ANNOTATOR;
    }

    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            public StructureViewModel createStructureViewModel() {
                return new JSStructureViewModel((JSElement) psiFile);
            }
        };
    }

    @NotNull
    public FindUsagesProvider getFindUsagesProvider() {
        return new LuaFindUsagesProvider();
    }

    public Commenter getCommenter() {
        return new LuaCommenter();
    }

    public FormattingModelBuilder getFormattingModelBuilder() {
        return new FormattingModelBuilder() {
            @NotNull
            public FormattingModel createModel(final PsiElement element, final CodeStyleSettings settings) {
                return new JSFormattingModel(element.getContainingFile(), settings, new JSBlock(element.getNode(), null, null, null, settings));
            }
        };
    }

    @NotNull
    public SurroundDescriptor[] getSurroundDescriptors() {
        return SURROUND_DESCRIPTORS;
    }
}
  
*/