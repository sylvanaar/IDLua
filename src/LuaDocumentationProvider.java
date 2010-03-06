package com.sylvanaar.idea.Lua;

import com.intellij.lang.documentation.QuickDocumentationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.sylvanaar.idea.Lua.psi.LuaDirectiveName;
import com.sylvanaar.idea.Lua.psi.LuaInnerVariable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 19.08.2009
 * Time: 0:49:41
 */
public class LuaDocumentationProvider extends QuickDocumentationProvider {

    public static final Logger LOG = Logger.getInstance("#com.intellij.lang.documentation.QuickDocumentationProvider");

    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {

        if (element instanceof LuaDirectiveName) {
            return generateDocForDirectiveName((LuaDirectiveName) element);
        } else if (element instanceof LuaInnerVariable) {
            return generateDocForInnerVariable((LuaInnerVariable) element);
        }
        return null;
    }

    private String generateDocForDirectiveName(LuaDirectiveName element) {

        StringBuilder result = new StringBuilder();
        InputStream docStream = getClass().getResourceAsStream("docs/directives/" + element.getText() + ".html");
        if (docStream == null) {
            result.append(LuaBundle.message("docs.directive.notfound", element.getText()));
        } else {
            BufferedReader keywordsReader = new BufferedReader(new InputStreamReader(docStream));
            try {
                String line;
                while ((line = keywordsReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            } catch (IOException e) {
                LOG.error(e);
                return null;
            } finally {
                try {
                    keywordsReader.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }

        return result.toString();
    }

    private String generateDocForInnerVariable(LuaInnerVariable element) {

        StringBuilder result = new StringBuilder();
        InputStream docStream = getClass().getResourceAsStream("docs/variables/" + element.getName() + ".html");
        if (docStream == null) {
            result.append(LuaBundle.message("docs.variable.notfound", element.getName()));
        } else {
            result.append("<b>").append(element.getText()).append("</b><br>");
            BufferedReader keywordsReader = new BufferedReader(new InputStreamReader(docStream));
            try {
                String line;
                while ((line = keywordsReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            } catch (IOException e) {
                LOG.error(e);
                return null;
            } finally {
                try {
                    keywordsReader.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }

        return result.toString();

    }

    public String getQuickNavigateInfo(PsiElement element) {
        return null;
    }

}
