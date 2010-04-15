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

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 14, 2010
 * Time: 12:45:19 AM
 */
public class LuaExternalAnnotator implements ExternalAnnotator {
    boolean enabled = true;

    @Override
    public void annotate(PsiFile file, AnnotationHolder holder) {
        ProcessBuilder pb = new ProcessBuilder("luac", "-p", file.getVirtualFile().getName());

        try {
            File dir = new File(file.getContainingDirectory().getVirtualFile().toString().substring(7));

            pb.directory(dir);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            String s = readStreamAsString(p.getInputStream());

            if (s.contains(file.getName())) {
                int start = s.indexOf(file.getName());
                start = s.indexOf(':', start) + 1;
                int end = s.indexOf(':', start);
                int line = Integer.parseInt(s.substring(start, end));

                int lstart = 0, lend = 0;
                lstart = file.getViewProvider().getDocument().getLineStartOffset(line - 1);
                lend = file.getViewProvider().getDocument().getLineEndOffset(line - 1);

                holder.createErrorAnnotation(new TextRange(lstart, lend), s.substring(end + 1));
            }

        } catch (IOException e) {
            enabled = false; // this works allright for  now
        }

    }


    private static String readStreamAsString(InputStream is) {
        final char[] buffer = new char[0x10000]; // luac should not output more than a few lines
        StringBuilder out = new StringBuilder();
        Reader in = null;
        try {
            in = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException e) {

        }
        int read = 0;
        do {
            try {
                read = in.read(buffer, 0, buffer.length);
            } catch (IOException e) {

            }
            if (read > 0) {
                out.append(buffer, 0, read);
            }
        } while (read >= 0);

        return out.toString();
    }
}
    

