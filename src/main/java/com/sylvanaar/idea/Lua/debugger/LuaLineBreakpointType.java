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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFileBase;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDoStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.util.LuaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/26/11
 * Time: 3:04 PM
 */
public class LuaLineBreakpointType extends XLineBreakpointType<XBreakpointProperties> {
    private static final Logger log = Logger.getInstance("Lua.LuaLineBreakpointType");

    private final LuaDebuggerEditorsProvider myEditorsProvider = new LuaDebuggerEditorsProvider();

    public LuaLineBreakpointType() {
        super("lua-line", "Lua Line Breakpoints");
    }

    @Nullable
    @Override
    public XBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        return null;
    }

    @Override
    public String getDisplayText(XLineBreakpoint<XBreakpointProperties> breakpoint) {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();

        assert sourcePosition != null;
        return "Line " + String.valueOf(sourcePosition.getLine()) +
               " in file " + LuaFileUtil.getPathToDisplay(sourcePosition.getFile());
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        // TODO: scan the line looking for a statement START
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        if (!(psiFile instanceof LuaPsiFile))
            return false;

        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);

        assert document != null;

        int start = document.getLineStartOffset(line);
        int end = document.getLineEndOffset(line);

        for (LuaStatementElement stat : ((LuaPsiFileBase) psiFile).getAllStatements()) {
            if (stat instanceof LuaDoStatement) return false;
            if (stat.getTextOffset() >= start && stat.getTextOffset() < end) return true;

        }
        return false;
    }

    @Nullable
    @Override
    public XDebuggerEditorsProvider getEditorsProvider(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint,
                                                       @NotNull Project project) {
        return myEditorsProvider;
    }
}
