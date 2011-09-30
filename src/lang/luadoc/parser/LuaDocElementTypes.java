/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

package com.sylvanaar.idea.Lua.lang.luadoc.parser;

import com.intellij.lang.*;
import com.intellij.lang.impl.PsiBuilderImpl;
import com.intellij.openapi.project.Project;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.luadoc.lexer.LuaDocElementType;
import com.sylvanaar.idea.Lua.lang.luadoc.lexer.LuaDocElementTypeImpl;
import com.sylvanaar.idea.Lua.lang.luadoc.lexer.LuaDocLexer;
import com.sylvanaar.idea.Lua.lang.luadoc.lexer.LuaDocTokenTypes;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.impl.LuaDocCommentImpl;
import org.jetbrains.annotations.NotNull;







/**
 * @author ilyas
 */
public interface LuaDocElementTypes extends LuaDocTokenTypes {

  /**
   * LuaDoc comment
   */
  ILazyParseableElementType LUADOC_COMMENT = new ILazyParseableElementType("LuaDocComment") {
    @NotNull
    public Language getLanguage() {
      return LuaFileType.LUA_FILE_TYPE.getLanguage();
    }

    public ASTNode parseContents(ASTNode chameleon) {

      final PsiElement parentElement = chameleon.getTreeParent().getPsi();

      assert parentElement != null;
        
      final Project project = parentElement.getProject();

      final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, new LuaDocLexer(), getLanguage(), chameleon.getText());
      final PsiParser parser = new LuaDocParser();

      return parser.parse(this, builder).getFirstChildNode();
    }

    @Override
    public ASTNode createNode(CharSequence text) {
      return new LuaDocCommentImpl(text);
    }
  };

  LuaDocElementType LDOC_TAG = new LuaDocElementTypeImpl("LuaDocTag");

  LuaDocElementType LDOC_REFERENCE_ELEMENT = new LuaDocElementTypeImpl("LuaDocReferenceElement");
  LuaDocElementType LDOC_PARAM_REF = new LuaDocElementTypeImpl("LuaDocParameterReference");
  LuaDocElementType LDOC_FIELD_REF = new LuaDocElementTypeImpl("LuaDocFieldReference");
  }
