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

package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.sylvanaar.idea.Lua.LuaIcons;
import com.sylvanaar.idea.Lua.options.LuaOptions;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;

public class LuaIdentifierHighlighterAppComponent implements ApplicationComponent, EditorFactoryListener {
    protected HashMap<Editor, LuaIdentifierHighlighterEditorComponent> _editorHighlighters = null;

    public void initComponent() {
        _editorHighlighters = new HashMap<Editor, LuaIdentifierHighlighterEditorComponent>();
        //Add listener for editors
        EditorFactory.getInstance().addEditorFactoryListener(this);
    }

    public void disposeComponent() {
        //Remove listener for editors
        EditorFactory.getInstance().removeEditorFactoryListener(this);
        for (LuaIdentifierHighlighterEditorComponent value : _editorHighlighters.values())
            value.dispose();
        _editorHighlighters.clear();
    }

    @NotNull
    public String getComponentName() {
        return ("LuaIdentifierHighlighterAppComponent");
    }

    //EditorFactoryListener interface implementation
    public void editorCreated(EditorFactoryEvent efe) {
        Editor editor = efe.getEditor();
        if (editor.getProject() == null)
            return;
        LuaIdentifierHighlighterEditorComponent editorHighlighter = new LuaIdentifierHighlighterEditorComponent(this, efe.getEditor());
        _editorHighlighters.put(efe.getEditor(), editorHighlighter);
    }

    public void editorReleased(EditorFactoryEvent efe) {
        LuaIdentifierHighlighterEditorComponent editorHighlighter = _editorHighlighters.remove(efe.getEditor());
        if (editorHighlighter == null)
            return;
        editorHighlighter.dispose();
    }

    //Configurable interface implementation
    public String getDisplayName() {
        return ("Identifier Highlighter");
    }

    public Icon getIcon() {
        return (LuaIcons.LUA_ICON);
    }

    public boolean isEnabled() {
        return LuaOptions.storedSettings().isIdentifierHilighting();
    }
}
