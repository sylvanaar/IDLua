/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.documentor;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocComment;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocCommentOwner;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocTag;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.LuaDocTagValueToken;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaAlias;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/1/11
 * Time: 12:28 AM
 */
public class LuaDocDocumentationProvider implements DocumentationProvider {
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return null;
    }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        return null;
    }

    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {
        final LuaFunctionDefinitionStatement functionDefinitionStatement =
                getParentOfType(element, LuaFunctionDefinitionStatement.class, true, LuaBlock.class);

        if (functionDefinitionStatement != null) {
            element = functionDefinitionStatement;
        } else if (element instanceof LuaAlias) {
            final PsiElement aliasElement = ((LuaAlias) element).getAliasElement();
            if (aliasElement != null && aliasElement instanceof LuaDocCommentOwner) element = aliasElement;
        }

        if (!(element instanceof LuaDocCommentOwner))
            return null;

        LuaDocComment docComment = ((LuaDocCommentOwner) element).getDocComment();
        if (docComment == null)
            return null;

        StringBuilder sb = new StringBuilder();

        sb.append("<html><head>" +
                "    <style type=\"text/css\">" +
                "        #error {" +
                "            background-color: #eeeeee;" +
                "            margin-bottom: 10px;" +
                "        }" +
                "        p {" +
                "            margin: 5px 0;" +
                "        }" +
                "    </style>" +
                "</head><body>");

        LuaDocCommentOwner owner = docComment.getOwner();
        if (owner != null) {
            String name = owner.getName();

            if (name != null)
                sb.append("<h2>").append(name).append("</h2>");
        }
        sb.append("<p class=description>");
        for (PsiElement e : docComment.getDescriptionElements())
            sb.append(e.getText()).append(' ');
        sb.append("</p>");

        buildTagValuesListSection("param", docComment, sb);
        buildTagValuesListSection("field", docComment, sb);
        buildTagListSection("return", docComment, sb);
        buildTagValuesListSection("retval", docComment, sb);
        buildTagListSection("see", docComment, sb);
        buildTagListSection("external", docComment, sb);
        buildTagSection("usage", docComment, sb);
        buildTagSection("release", docComment, sb);
        buildTagSection("author", docComment, sb);
        buildTagSection("copyright", docComment, sb);

        sb.append("</body></html>");
        return sb.toString();
    }

    private void buildSectionHeader(String section, LuaDocComment docComment, StringBuilder sb) {
        String sectionTitle = getSectionTitle(section);
        if (sectionTitle == null) return;
        sb.append("<b>").append(sectionTitle).append("</b>");
    }

    private void buildTagSection(String section, LuaDocComment docComment, StringBuilder sborig) {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        buildSectionHeader(section, docComment, sb);

        sb.append("<p class=").append(section).append('>');

        for (LuaDocTag tag : docComment.getTags()) {
            if (!tag.getName().equals(section)) continue;
            count++;
            for (PsiElement desc : tag.getDescriptionElements())
                sb.append(desc.getText());
        }

        sb.append("</p>");

        if (count > 0)
            sborig.append(sb);
    }

    private void buildTagListSection(String section, LuaDocComment docComment, StringBuilder sborig) {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        buildSectionHeader(section, docComment, sb);

        sb.append("<ul class=").append(section).append('>');

        for (LuaDocTag tag : docComment.getTags()) {
            if (!tag.getName().equals(section)) continue;
            count++;

            for (PsiElement desc : tag.getDescriptionElements())
                sb.append("<li>").append(desc.getText()).append("</li>");
        }

        sb.append("</ul>");

        if (count > 0)
            sborig.append(sb);
    }

    private void buildTagValuesListSection(String section, LuaDocComment docComment, StringBuilder sborig) {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        buildSectionHeader(section, docComment, sb);

        sb.append("<dl class=").append(section).append('>');

        for (LuaDocTag tag : docComment.getTags()) {
            if (!tag.getName().equals(section)) continue;

            LuaDocTagValueToken value = tag.getValueElement();
            if (value == null) continue;
            count++;

            sb.append("<dt>").append(value.getText()).append("</dt>");

            for (PsiElement desc : tag.getDescriptionElements())
                sb.append("<dd>").append(desc.getText()).append("</dd>");
        }

        sb.append("</dl>");

        if (count > 0)
            sborig.append(sb);
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null;
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null;
    }

    @Nullable
    private String getSectionTitle(@NotNull String section) {
        if (section.equals("param"))
            return "Parameters";
        else if (section.equals("return"))
            return "Returns";
        else if (section.equals("retval"))
            return "Return Values";
        else if (section.equals("field"))
            return "Fields";
        else if (section.equals("see"))
            return "See Also";
        else if (section.equals("usage"))
            return "Usage";
        else if (section.equals("release"))
            return "Release";
        else if (section.equals("author"))
            return "Author";
        else if (section.equals("copyright"))
            return "Copyright";
        else if (section.equals("external"))
            return "External Links";
        return null;
    }
}
