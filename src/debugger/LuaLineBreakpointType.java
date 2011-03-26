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

package com.sylvanaar.idea.Lua.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/26/11
 * Time: 3:04 PM
 */
public class LuaLineBreakpointType extends XLineBreakpointType {
    private static final Logger log = Logger.getInstance("#Lua.LuaLineBreakpointType");
    
    protected LuaLineBreakpointType() {
        super("lua-line", "Breakpoint");
    }

    @Override
    public XBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        log.info("breakpoint " + file + ' ' + line);
        
        return new XBreakpointProperties() {
            @Override
            public Object getState() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void loadState(Object state) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    @Override
    public String getDisplayText(XBreakpoint breakpoint) {
        return "Test";
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        log.info("breakpoint " + file + ' ' + line);

        // TODO: scan the line looking for a statement START
        LuaPsiFile psiFile = (LuaPsiFile) PsiManager.getInstance(project).findFile(file);

        assert psiFile != null;

        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);

        assert document != null;

        int start = document.getLineStartOffset(line);
        int end = document.getLineEndOffset(line);

        for (LuaStatementElement stat : psiFile.getStatements())
            if (stat.getTextOffset() >= start && stat.getTextOffset() < end)
                return true;

        return false;
    }
}
