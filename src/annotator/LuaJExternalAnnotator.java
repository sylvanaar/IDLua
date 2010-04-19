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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.luaj.compiler.LuaC;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 14, 2010
 * Time: 12:45:19 AM
 */
public class LuaJExternalAnnotator implements ExternalAnnotator {
    boolean enabled = true;

    static LuaJExternalAnnotator INSTANCE;

    public static LuaJExternalAnnotator getInstance() { if (INSTANCE == null) INSTANCE =
            new LuaJExternalAnnotator();  return INSTANCE; }

    private LuaJExternalAnnotator() {
            Platform.setInstance(new J2sePlatform());
		    LuaC.install();
    }

    @Override
    public void annotate(PsiFile file, AnnotationHolder holder) {
        if (!enabled) return;
        
        try {
            File dir = new File(file.getContainingDirectory().getVirtualFile().toString().substring(7));

            File script = new File(dir, file.getVirtualFile().getName());

            assert(script.exists());

            FileInputStream fis = new FileInputStream(script.getAbsolutePath());

            file.getViewProvider().getDocument().getText();
            
            while (fis.available()>0) {
                try {
                    compileScript(fis, script.getName());
                } catch (LuaErrorException err) {
                    makeErrorAnnotation(file, holder, err.getMessage());
                }
            }
        }
        catch (Throwable t) {
            //enabled = false;
        }
    }

    private void makeErrorAnnotation(PsiFile file, AnnotationHolder holder, String s) {
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
    }

    private void compileScript( InputStream script, String chunkname ) throws IOException {
		try {
	        // create the chunk
            org.luaj.compiler.LuaC.compile(script, chunkname);
		} finally {
			script.close();
		}
    }
}