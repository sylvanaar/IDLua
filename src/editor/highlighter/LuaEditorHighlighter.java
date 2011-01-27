package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.LuaFileType;

public class LuaEditorHighlighter extends LayeredLexerEditorHighlighter {

  public LuaEditorHighlighter(EditorColorsScheme scheme, Project project, VirtualFile virtualFile) {
    super(LuaSyntaxHighlighterFactory.getSyntaxHighlighter(LuaFileType.LUA_LANGUAGE, project, virtualFile), scheme);
   }

  
}
