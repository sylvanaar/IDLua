package com.sylvanaar.idea.Lua.lang.psi;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.ICodeFragmentElementType;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.parser.kahlua.KahluaParser;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Jon on 10/29/2016.
 */
public class LuaExpressionFragmentElementType extends ICodeFragmentElementType {
    public LuaExpressionFragmentElementType() {
        super("EXPRESSION_FRAGMENT", LuaFileType.LUA_LANGUAGE);
    }

    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
        Project project = psi.getProject();
        Language languageForParser = getLanguageForParser(psi);
        PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, chameleon.getChars());
        KahluaParser parser = (KahluaParser) LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(project);
        ASTNode node = parser.parseExpressionFragment(this, builder);
        return node.getFirstChildNode();
    }
}
