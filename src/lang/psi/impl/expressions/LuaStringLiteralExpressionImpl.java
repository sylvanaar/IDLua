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

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/7/11
 * Time: 12:18 PM
 */
public class LuaStringLiteralExpressionImpl extends LuaLiteralExpressionImpl {
    public LuaStringLiteralExpressionImpl(ASTNode node) {
        super(node);
    }


    @Override
    public Object getValue() {
        return getStringContent();
    }

    public String getStringContent() {
        return stripQuotes(getText());
    }


    @Override
    public LuaType getLuaType() {
        return LuaType.STRING;
    }


    public static String stripQuotes(String text) {
        switch (text.charAt(0)) {
            case '\'':
            case '\"':
                return text.substring(1, text.length()-1);

            case '[':
                int quoteLen = text.indexOf('[', 1);
                assert quoteLen > 1;

                return text.substring(quoteLen, text.length()-2*quoteLen-1);
        }

        return "ERROR";
    }
}
