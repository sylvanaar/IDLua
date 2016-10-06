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

import com.intellij.lang.ASTNode;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.LuaDocElementTypes;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.LuaDocElementWithDescriptions;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.api.*;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBlock;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaFunctionDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaAlias;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pegdown.PegDownProcessor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/1/11
 * Time: 12:28 AM
 */
public class LuaDocDocumentationProvider implements DocumentationProvider {
    public static final PegDownProcessor MARKDOWN = new PegDownProcessor();

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
                "        dt {" +
                "            font-family: monospace;" +
                "            white-space: pre;" +
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
        String markdown = markdownDescription(docComment);
        markdown = unwrapCode(markdown);
        sb.append(markdown);

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

    private String markdownDescription(LuaDocElementWithDescriptions element) {
        // Collect all of the description text content
        StringBuilder sb = new StringBuilder();
        for (PsiElement desc : element.getDescriptionElements()) {
            String descText = desc.getText();
            if (descText.length() > 0) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(descText);
            }

            sb.append(trailingNewlines(desc));
        }

        // convert it from markdown to html
        String source = sb.toString();
        if (StringUtil.isEmptyOrSpaces(source))
            return "";

        return MARKDOWN.markdownToHtml(source);
    }

    // Hack in newlines since markdown is newline-sensitive
    private String trailingNewlines(PsiElement desc) {
        PsiElement next = desc.getNextSibling();
        int newLines = 0;
        while (next != null) {
            if (!(next instanceof ASTNode)) break;
            ASTNode ast = (ASTNode)next;
            IElementType astType = ast.getElementType();
            if (astType == LuaDocElementTypes.LDOC_DASHES)
                newLines++;
            else if (astType == LuaDocElementTypes.LDOC_COMMENT_DATA)
                break;
            next = next.getNextSibling();
        }

        StringBuilder sb = new StringBuilder();
        while (newLines > 1) {
            sb.append("\n");
            newLines--;
        }
        return sb.toString();
    }

    // IDEA doesn't respect "white-space: pre;" css property
    private String unwrapCode(String html) {
        Pattern fencedCodePattern = Pattern.compile("<p><code>([^<]+)</code></p>");
        Matcher fencedCodeMatcher = fencedCodePattern.matcher(html);
        while (fencedCodeMatcher.matches()) {
            String outer = fencedCodeMatcher.group(0);
            String inner = fencedCodeMatcher.group(1);
            html = html.replace(outer, "<pre><code>" + inner + "</code></pre>");
            fencedCodeMatcher = fencedCodePattern.matcher(html);
        }
        return html;
    }

    // Sometimes we don't want a paragraph.
    private String unwrapPara(String html) {
        if (html.startsWith("<p>"))
            html = html.substring(3);
        if (html.endsWith("</p>"))
            html = html.substring(0, html.length() - 4);
        return html;
    }

    private void buildSectionHeader(String section, LuaDocComment docComment, StringBuilder sb) {
        String sectionTitle = getSectionTitle(section);
        if (sectionTitle == null) return;
        sb.append("<h3>").append(sectionTitle).append("</h3>");
    }

    private void buildTagSection(String section, LuaDocComment docComment, StringBuilder sborig) {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        buildSectionHeader(section, docComment, sb);

        for (LuaDocTag tag : docComment.getTags()) {
            if (!tag.getName().equals(section)) continue;
            count++;
            String markdown = unwrapCode(markdownDescription(tag));
            sb.append(markdown);
        }

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

            sb.append("<li>");
            String markdown = markdownDescription(tag);
            markdown = unwrapPara(markdown);
            markdown = unwrapCode(markdown);
            sb.append(markdown);
            sb.append("</li>");
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
            sb.append("<dd>").append(markdownDescription(tag)).append("</dd>");
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
