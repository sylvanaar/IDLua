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

package com.sylvanaar.idea.Lua.lang.formatter;

import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.sylvanaar.idea.Lua.LuaFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 19, 2010
 * Time: 7:49:02 PM
 */
public class CodeStyleLuaPanel extends CodeStyleAbstractPanel {
    private JPanel myPreviewPanel;
    private JComboBox myWrapAttributes;
    private JPanel myPanel;

    public CodeStyleLuaPanel(CodeStyleSettings settings) {  
        super(settings);
        installPreviewPanel(myPreviewPanel);
    
      //  fillWrappingCombo(myWrapAttributes);
    
        addPanelToWatch(myPanel);   
    }

    @Override
    protected EditorHighlighter createHighlighter(EditorColorsScheme scheme) {
        return EditorHighlighterFactory.getInstance().createEditorHighlighter(getFileType(), scheme, null);
    }

    @Override
    protected int getRightMargin() {
        return 60;
    }

    @Override
    protected void prepareForReformat(PsiFile psiFile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    protected FileType getFileType() {
        return LuaFileType.LUA_FILE_TYPE;
    }

    @Override
    protected String getPreviewText() {
        return "local foo\n" +
                "\n" +
                "local foo = foo\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "-- I am testing globals/locals\n" +
                "\n" +
                "function a(b,c,d)\n" +
                "  a,b = c,d\n" +
                "  return self\n" +
                "end\n" +
                "\n" +
                "local function a()\n" +
                "  a,b = c,d\n" +
                "  return self\n" +
                "end\n" +
                "\n" +
                "local t = {}\n" +
                "function t:b(c,d)\n" +
                "  return self\n" +
                "end\n" +
                "\n" +
                "local foo = foo\n" +
                "\n" +
                "local a = function()\n" +
                "    return\n" +
                "end\n" +
                "\n" +
                "b.c = function()\n" +
                "    return \n" +
                "end\n" +
                "\n" +
                "a.b.c.d = function()\n" +
                "    return\n" +
                "end\n" +
                "\n" +
                "\n" +
                "local a,b,c,d,e,f = 1,2,3,4\n" +
                "\n" +
                "\n" +
                "for k,v in pairs(t) do\n" +
                "end\n" +
                "\n" +
                "for i=1,10 do\n" +
                "end\n" +
                "\n" +
                "local x1 = function()\n" +
                "        print( \"local disappear: \" .. tostring(x1) )            -- prints \"nil\"\n" +
                "end\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "        ";
    }

    @Override
    public void apply(CodeStyleSettings settings) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isModified(CodeStyleSettings settings) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JComponent getPanel() {
        return myPanel;
    }

    @Override
    protected void resetImpl(CodeStyleSettings settings) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
