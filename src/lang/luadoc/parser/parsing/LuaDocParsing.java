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
import org.jetbrains.annotations.NonNls;



/**
* @author ilyas
*/
public class LuaDocParsing implements LuaDocElementTypes {


  static enum RESULT {
    ERROR, METHOD, FIELD
  }

  @NonNls
  private static final String SEE_TAG = "@see";
  @NonNls
  private static final String PARAM_TAG = "@param";

  private final static TokenSet REFERENCE_BEGIN = TokenSet.create(LDOC_TAG_VALUE);

  private int myBraceCounter = 0;


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
        }

        while (!timeToEnd(builder)) {
            if (LDOC_TAG_NAME == builder.getTokenType()) {
                marker.done(LDOC_TAG);
                return true;
            } else {
                builder.advanceLexer();
            }
        }
        marker.done(LDOC_TAG);

        return true;
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
        IElementType type = builder.getTokenType();
        if (!REFERENCE_BEGIN.contains(type)) return false;
        PsiBuilder.Marker marker = builder.mark();
        if (LDOC_TAG_VALUE == type) {
            builder.advanceLexer();
        }
        marker.drop();
        return true;
    }

//    private boolean parseField(PsiBuilder builder) {
//    if (builder.getTokenType() != LDOC_TAG_VALUE) return ERROR;
//    builder.advanceLexer();
//    PsiBuilder.Marker params = builder.mark();
//    if (LDOC_TAG_VALUE_LPAREN != builder.getTokenType()) {
//      params.drop();
//      return FIELD;
//    }
//    builder.advanceLexer();
//    while (parseMethodParameter(builder) && !timeToEnd(builder)) {
//      while (LDOC_TAG_VALUE_COMMA != builder.getTokenType() &&
//              LDOC_TAG_VALUE_RPAREN != builder.getTokenType() &&
//              !timeToEnd(builder)) {
//        builder.advanceLexer();
//      }
//      while (LDOC_TAG_VALUE_COMMA == builder.getTokenType()) {
//        builder.advanceLexer();
//      }
//    }
//    if (builder.getTokenType() == LDOC_TAG_VALUE_RPAREN) {
//      builder.advanceLexer();
//    }
//    params.done(LDOC_METHOD_PARAMS);
//    return METHOD;
//  }

//  private boolean parseReferenceOrType(PsiBuilder builder) {
//    IElementType type = builder.getTokenType();
//    if (LDOC_TAG_VALUE != type) return false;
//    return true;
//  }


}
