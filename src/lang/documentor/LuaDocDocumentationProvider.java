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

        if (element instanceof LuaDocCommentOwner) {
            LuaDocComment docComment = ((LuaDocCommentOwner) element).getDocComment();
            if (docComment != null) {
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

                buildTagListSection("param", docComment, sb);
                buildTagListSection("field", docComment, sb);
                buildTagSection("returns", docComment, sb);

                sb.append("</body></html>");
                return sb.toString();
            }
        }

        return null;
    }

    private void buildTagSection(String section, LuaDocComment docComment, StringBuilder sb) {
        sb.append("<p class=").append(section).append('>');

        for (LuaDocTag tag : docComment.getTags()) {
            if (!tag.getName().equals(section)) continue;

            for (PsiElement desc : tag.getDescriptionElements())
                sb.append(desc.getText());
        }

        sb.append("</p>");
    }

    private void buildTagListSection(String section, LuaDocComment docComment, StringBuilder sb) {
        sb.append("<dl class=").append(section).append('>');

        for (LuaDocTag tag : docComment.getTags()) {
            if (!tag.getName().equals(section)) continue;

            LuaDocTagValueToken value = tag.getValueElement();
            if (value == null) continue;
            sb.append("<dt>").append(value.getText()).append("</dt>");

            for (PsiElement desc : tag.getDescriptionElements())
                sb.append("<dd>").append(desc.getText()).append("</dd>");
        }

        sb.append("</dl>");
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null;
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null;
    }
}
