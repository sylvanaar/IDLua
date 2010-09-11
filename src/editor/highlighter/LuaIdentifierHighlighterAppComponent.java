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
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.*;
import com.sylvanaar.idea.Lua.LuaIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;

public class LuaIdentifierHighlighterAppComponent implements ApplicationComponent, EditorFactoryListener {
//  public static final String DEFAULT_CLASS_ACTIVE_HIGHLIGHT_COLOR = "0,175,175";
//  public static final String DEFAULT_CLASS_HIGHLIGHT_COLOR = "128,255,255";
//  public static final String DEFAULT_METHOD_ACTIVE_HIGHLIGHT_COLOR = "0,175,175";
//  public static final String DEFAULT_METHOD_HIGHLIGHT_COLOR = "128,255,255";
//  public static final String DEFAULT_FIELD_READ_ACTIVE_HIGHLIGHT_COLOR = "0,175,175";
//  public static final String DEFAULT_FIELD_READ_HIGHLIGHT_COLOR = "128,255,255";
//  public static final String DEFAULT_PARAM_READ_ACTIVE_HIGHLIGHT_COLOR = "0,175,175";
//  public static final String DEFAULT_PARAM_READ_HIGHLIGHT_COLOR = "128,255,255";
//  public static final String DEFAULT_LOCAL_READ_ACTIVE_HIGHLIGHT_COLOR = "0,175,175";
//  public static final String DEFAULT_LOCAL_READ_HIGHLIGHT_COLOR = "128,255,255";
//  public static final String DEFAULT_FIELD_WRITE_ACTIVE_HIGHLIGHT_COLOR = "175,0,0";
//  public static final String DEFAULT_FIELD_WRITE_HIGHLIGHT_COLOR = "255,128,128";
//  public static final String DEFAULT_PARAM_WRITE_ACTIVE_HIGHLIGHT_COLOR = "175,0,0";
//  public static final String DEFAULT_PARAM_WRITE_HIGHLIGHT_COLOR = "255,128,128";
//  public static final String DEFAULT_LOCAL_WRITE_ACTIVE_HIGHLIGHT_COLOR = "175,0,0";
//  public static final String DEFAULT_LOCAL_WRITE_HIGHLIGHT_COLOR = "255,128,128";
//  public static final String DEFAULT_OTHER_ACTIVE_HIGHLIGHT_COLOR = "0,175,175";
//  public static final String DEFAULT_OTHER_HIGHLIGHT_COLOR = "128,255,255";
//  public static final String DEFAULT_HIGHLIGHT_LAYER = "ADDITIONAL_SYNTAX";
//  public static final boolean DEFAULT_CLASS_HIGHLIGHT_ENABLED = true;
//  public static final boolean DEFAULT_METHOD_HIGHLIGHT_ENABLED = true;
//  public static final boolean DEFAULT_FIELD_HIGHLIGHT_ENABLED = true;
//  public static final boolean DEFAULT_PARAM_HIGHLIGHT_ENABLED = true;
//  public static final boolean DEFAULT_LOCAL_HIGHLIGHT_ENABLED = true;
//  public static final boolean DEFAULT_OTHER_HIGHLIGHT_ENABLED = true;
//  public static final boolean DEFAULT_SHOW_IN_MARKER_BAR = true;
//  public static final boolean DEFAULT_PLUGIN_ENABLED = true;

  protected HashMap<Editor,LuaIdentifierHighlighterEditorComponent> _editorHighlighters = null;
  
//  protected Icon _highlightIcon = IconLoader.getIcon("/com/lgc/identifierhighlighter/images/highlighter_24.png");
//  public boolean _classHighlightEnabled = DEFAULT_CLASS_HIGHLIGHT_ENABLED;
//  public boolean _methodHighlightEnabled = DEFAULT_METHOD_HIGHLIGHT_ENABLED;
//  public boolean _fieldHighlightEnabled = DEFAULT_FIELD_HIGHLIGHT_ENABLED;
//  public boolean _paramHighlightEnabled = DEFAULT_PARAM_HIGHLIGHT_ENABLED;
//  public boolean _localHighlightEnabled = DEFAULT_LOCAL_HIGHLIGHT_ENABLED;
//  public boolean _otherHighlightEnabled = DEFAULT_OTHER_HIGHLIGHT_ENABLED;
//  public boolean _showInMarkerBar = DEFAULT_SHOW_IN_MARKER_BAR;
//  public boolean _pluginEnabled = DEFAULT_PLUGIN_ENABLED;
//  public String _classActiveHighlightColor = DEFAULT_CLASS_ACTIVE_HIGHLIGHT_COLOR;
//  public String _classHighlightColor = DEFAULT_CLASS_HIGHLIGHT_COLOR;
//  public String _methodActiveHighlightColor = DEFAULT_METHOD_ACTIVE_HIGHLIGHT_COLOR;
//  public String _methodHighlightColor = DEFAULT_METHOD_HIGHLIGHT_COLOR;
//  public String _fieldReadActiveHighlightColor = DEFAULT_FIELD_READ_ACTIVE_HIGHLIGHT_COLOR;
//  public String _fieldReadHighlightColor = DEFAULT_FIELD_READ_HIGHLIGHT_COLOR;
//  public String _paramReadActiveHighlightColor = DEFAULT_PARAM_READ_ACTIVE_HIGHLIGHT_COLOR;
//  public String _paramReadHighlightColor = DEFAULT_PARAM_READ_HIGHLIGHT_COLOR;
//  public String _localReadActiveHighlightColor = DEFAULT_LOCAL_READ_ACTIVE_HIGHLIGHT_COLOR;
//  public String _localReadHighlightColor = DEFAULT_LOCAL_READ_HIGHLIGHT_COLOR;
//  public String _fieldWriteActiveHighlightColor = DEFAULT_FIELD_WRITE_ACTIVE_HIGHLIGHT_COLOR;
//  public String _fieldWriteHighlightColor = DEFAULT_FIELD_WRITE_HIGHLIGHT_COLOR;
//  public String _paramWriteActiveHighlightColor = DEFAULT_PARAM_WRITE_ACTIVE_HIGHLIGHT_COLOR;
//  public String _paramWriteHighlightColor = DEFAULT_PARAM_WRITE_HIGHLIGHT_COLOR;
//  public String _localWriteActiveHighlightColor = DEFAULT_LOCAL_WRITE_ACTIVE_HIGHLIGHT_COLOR;
//  public String _localWriteHighlightColor = DEFAULT_LOCAL_WRITE_HIGHLIGHT_COLOR;
//  public String _otherActiveHighlightColor = DEFAULT_OTHER_ACTIVE_HIGHLIGHT_COLOR;
//  public String _otherHighlightColor = DEFAULT_OTHER_HIGHLIGHT_COLOR;
//  public String _highlightLayer = DEFAULT_HIGHLIGHT_LAYER;

  public void initComponent()
  {
    _editorHighlighters = new HashMap<Editor, LuaIdentifierHighlighterEditorComponent>();
    //Add listener for editors
    EditorFactory.getInstance().addEditorFactoryListener(this);
  }

  public void disposeComponent()
  {
    //Remove listener for editors
    EditorFactory.getInstance().removeEditorFactoryListener(this);
    for(LuaIdentifierHighlighterEditorComponent value : _editorHighlighters.values())
      value.dispose();
    _editorHighlighters.clear();
  }

  @NotNull
  public String getComponentName()
  {
    return("LuaIdentifierHighlighterAppComponent");
  }

  //EditorFactoryListener interface implementation
  public void editorCreated(EditorFactoryEvent efe)
  {
    Editor editor = efe.getEditor();
    if(editor.getProject() == null)
      return;
    LuaIdentifierHighlighterEditorComponent editorHighlighter = new LuaIdentifierHighlighterEditorComponent(this,efe.getEditor());
    _editorHighlighters.put(efe.getEditor(),editorHighlighter);
  }

  public void editorReleased(EditorFactoryEvent efe)
  {
    LuaIdentifierHighlighterEditorComponent editorHighlighter = _editorHighlighters.remove(efe.getEditor());
    if(editorHighlighter == null)
      return;
    editorHighlighter.dispose();
  }

  //Configurable interface implementation
  public String getDisplayName()
  {
    return("Identifier Highlighter");
  }

  public Icon getIcon()
  {
    return(LuaIcons.LUA_ICON);
  }

//  public String getHelpTopic()
//  {
//    return("doc-ih");
//  }
//
//  //JDOMExternalizable interface implementation
//  public void readExternal(Element element) throws InvalidDataException
//  {
//    DefaultJDOMExternalizer.readExternal(this, element);
//  }
//
//  public void writeExternal(Element element) throws WriteExternalException
//  {
//    DefaultJDOMExternalizer.writeExternal(this, element);
//  }

}
