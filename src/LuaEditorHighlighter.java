package com.sylvanaar.idea.Lua;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.sylvanaar.idea.Lua.lexer.LuaSyntaxHighlighter;


/**
 * @author ilyas
 */
public class LuaEditorHighlighter extends LayeredLexerEditorHighlighter {

  public LuaEditorHighlighter(EditorColorsScheme scheme, Project project, VirtualFile virtualFile) {
    super(new LuaSyntaxHighlighter(), scheme);
   }

  
}
