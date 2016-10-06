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

package com.sylvanaar.idea.Lua.lang.luadoc.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.LuaDocElementTypes;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.elements.LuaDocTagValueTokenType;
import com.sylvanaar.idea.Lua.lang.luadoc.psi.impl.*;

import static com.sylvanaar.idea.Lua.lang.luadoc.parser.elements.LuaDocTagValueTokenType.TagValueTokenType.REFERENCE_ELEMENT;


/**
 * @author ilyas
 */
public class LuaDocPsiCreator implements LuaDocElementTypes {

  public static PsiElement createElement(ASTNode node) {
    IElementType type = node.getElementType();

    if (type instanceof LuaDocTagValueTokenType) {
      LuaDocTagValueTokenType value = (LuaDocTagValueTokenType) type;
      LuaDocTagValueTokenType.TagValueTokenType valueType = value.getValueType(node);
      if (valueType == REFERENCE_ELEMENT) return new LuaDocSymbolReferenceElementImpl(node);

      return new LuaDocTagValueTokenImpl(node);
    }

    if (type == LDOC_TAG) return new LuaDocTagImpl(node);
    if (type == LDOC_FIELD_REF) return new LuaDocFieldReferenceImpl(node);
    if (type == LDOC_PARAM_REF) return new LuaDocParameterReferenceImpl(node);
    if (type == LDOC_REFERENCE_ELEMENT) return new LuaDocSymbolReferenceElementImpl(node);

    return new ASTWrapperPsiElement(node);
  }
}
