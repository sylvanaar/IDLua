/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.lang.documentor;

import com.intellij.codeInsight.javadoc.JavaDocExternalFilter;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/9/11
 * Time: 4:27 AM
 */
public class LuaDocsExternalFilter extends JavaDocExternalFilter {
    @NonNls
    private static final String HR = "<HR>";
    @NonNls
    private static final String P = "<P>";
    @NonNls
    private static final String DL = "<DL>";
    @NonNls
    protected static final String H2 = "</H2>";
    @NonNls
    protected static final String HTML_CLOSE = "</HTML>";
    @NonNls
    protected static final String HTML = "<HTML>";
    @NonNls
    private static final String BR = "<BR>";
    @NonNls
    private static final String DT = "<DT>";
    private static
    @NonNls
    final Pattern ourHREFselector = Pattern.compile("<A.*?HREF=\"([^>\"]*)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public LuaDocsExternalFilter(Project project) {
        super(project);
        final Project project2 = project;

        getRefConvertors()[0] = new RefConvertor(ourHREFselector) {
            protected String convertReference(String root, String href) {
                if (BrowserUtil.isAbsoluteURL(href)) {
                    return href;
                }

                if (StringUtil.startsWithChar(href, '#')) {
                    return DOC_ELEMENT_PROTOCOL+href.substring(1);
                }

                return href;
            }
        };
    }

    protected void doBuildFromStream(String surl, Reader input, StringBuffer data) throws IOException {
        final BufferedReader buf = new BufferedReader(input);
        Matcher anchorMatcher = ourAnchorsuffix.matcher(surl);
        @NonNls String startSection = "<!-- ======== START OF CLASS DATA ======== -->";
        @NonNls String endSection = "SUMMARY ========";
        @NonNls String greatestEndSection = "<!-- ========= END OF CLASS DATA ========= -->";
        boolean isClassDoc = true;

        if (anchorMatcher.find()) {
            isClassDoc = false;
            startSection = "<A NAME=\"" + anchorMatcher.group(1).toUpperCase() + "\"";
            endSection = "<HR>";
        }

        data.append(HTML);

        String read;

        do {
            read = buf.readLine();
        }
        while (read != null && read.toUpperCase().indexOf(startSection) == -1);


        if (read == null) {
            data.delete(0, data.length());
            return;
        }

     //   if (read.toUpperCase().indexOf(HR) == -1) {
            appendLine(data, read);
      //  }

        while (((read = buf.readLine()) != null) && read.indexOf(endSection) == -1 && read.indexOf(greatestEndSection) == -1) {
            if (read.toUpperCase().indexOf(HR) == -1) {
                appendLine(data, read);
            } else {
                break;
            }
        }

        data.append(HTML_CLOSE);
    }

    private static void appendLine(final StringBuffer buffer, final String read) {
        buffer.append(read);
        buffer.append("\n");
    }

}
