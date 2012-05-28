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

package com.sylvanaar.idea.Lua.lang.luadoc.parser.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.LuaDocElementTypes;
import com.sylvanaar.idea.Lua.lang.luadoc.parser.elements.LuaDocTagValueTokenType;
import org.jetbrains.annotations.NonNls;



/**
* @author ilyas
*/
public class LuaDocParsing implements LuaDocElementTypes {
  @NonNls
  private static final String SEE_TAG = "@see";
  @NonNls
  private static final String PARAM_TAG = "@param";
  private static final String FIELD_TAG = "@field";
  private static final String NAME_TAG = "@name";

  private final static TokenSet REFERENCE_BEGIN = TokenSet.create(LDOC_TAG_VALUE);

  public boolean parse(PsiBuilder builder) {
    while (parseDataItem(builder)) ;
    return true;
  }

  /**
   * Parses doc comment at toplevel
   *
   * @param builder given builder
   * @return false in case of commnet end
   */
  private boolean parseDataItem(PsiBuilder builder) {
    if (timeToEnd(builder)) return false;

    if (LDOC_TAG_NAME == builder.getTokenType()) {
      parseTag(builder);
    } else {
      builder.advanceLexer();
    }

    return true;
  }

  private static boolean timeToEnd(PsiBuilder builder) {
    return builder.eof();
  }

    private boolean parseTag(PsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();

        assert builder.getTokenType() == LDOC_TAG_NAME;
        String tagName = builder.getTokenText();
        builder.advanceLexer();

        if (SEE_TAG.equals(tagName)) {
            parseSeeOrLinkTagReference(builder);
        } else if (PARAM_TAG.equals(tagName)) {
            parseParamTagReference(builder);
        } else if (FIELD_TAG.equals(tagName)) {
            parseFieldReference(builder);
        } else if (NAME_TAG.equals(tagName)) {
            parseNameReference(builder);
        } else if (builder.getTokenType() instanceof LuaDocTagValueTokenType) {
            builder.advanceLexer();
        }

        PsiBuilder.Marker lastdata = builder.mark();
        int start = builder.getCurrentOffset();
        while (!timeToEnd(builder)) {
            if (LDOC_TAG_NAME == builder.getTokenType() && builder.getCurrentOffset() != start) {
                lastdata.rollbackTo();
                marker.done(LDOC_TAG);
                return true;
            } else if (LDOC_COMMENT_DATA == builder.getTokenType()) {
                lastdata.drop();
                builder.advanceLexer();
                lastdata = builder.mark();
            } else {
                builder.advanceLexer();
            }
        }
        lastdata.drop();
        marker.done(LDOC_TAG);

        return true;
    }

    private boolean parseNameReference(PsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        if (LDOC_TAG_VALUE == builder.getTokenType()) {
            builder.advanceLexer();
            marker.done(LDOC_REFERENCE_ELEMENT);
            return true;
        }
        marker.drop();
        return false;
    }

    private boolean parseParamTagReference(PsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        if (LDOC_TAG_VALUE == builder.getTokenType()) {
            builder.advanceLexer();
            marker.done(LDOC_PARAM_REF);
            return true;
        }
        marker.drop();
        return false;
    }

    private boolean parseSeeOrLinkTagReference(PsiBuilder builder) {
//        IElementType type = builder.getTokenType();
//        if (!REFERENCE_BEGIN.contains(type)) return false;
//        PsiBuilder.Marker marker = builder.mark();
//        if (LDOC_TAG_VALUE == type) {
//            builder.advanceLexer();
//        }
        PsiBuilder.Marker marker = builder.mark();
        if (LDOC_TAG_VALUE == builder.getTokenType()) {
            builder.advanceLexer();
            marker.done(LDOC_REFERENCE_ELEMENT);
            return true;
        }

        marker.drop();
        return true;
    }


    private boolean parseFieldReference(PsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        if (LDOC_TAG_VALUE == builder.getTokenType()) {
            builder.advanceLexer();
            marker.done(LDOC_FIELD_REF);
            return true;
        }
        marker.drop();
        return false;
    }

    private boolean parseReferenceOrType(PsiBuilder builder) {
        IElementType type = builder.getTokenType();
        if (LDOC_TAG_VALUE != type) return false;
        return true;
    }


}
