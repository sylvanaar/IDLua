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

package com.sylvanaar.idea.Lua.editor.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.util.*;
import com.intellij.codeInsight.lookup.*;
import com.intellij.lang.*;
import com.intellij.lang.refactoring.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.text.*;
import com.intellij.openapi.vfs.*;
import com.sylvanaar.idea.Lua.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 16, 2010
 * Time: 10:50:28 AM
 */
public class LuaLookupElement extends LookupElement {
    private String str;
    private boolean typeInfered = false;
    private Object obj;


    public LuaLookupElement(String str) {
        this.str = str;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);
        presentation.setIcon(LuaIcons.LUA_ICON);
    }

    public LuaLookupElement(String str, boolean typeInfered) {
        this.str = str;
        this.typeInfered = typeInfered;
    }

    public LuaLookupElement(LuaDeclarationExpression symbol, boolean typeInfered) {
        this.str = StringUtil.notNullize(symbol.getDefinedName(), symbol.getText());
        this.obj = symbol;
        this.typeInfered = typeInfered;
    }

    @NotNull
    @Override
    public Object getObject() {
        if (obj == null)
            return super.getObject();

        return obj;
    }

    @NotNull
    public String getLookupString() {
        return str;
    }

    public static LookupElement createElement(LuaDeclarationExpression symbol) {
        final String name = symbol.getDefinedName();

        return createElement(symbol, name != null ? name : symbol.getText());
    }
    public static LookupElement createElement(LuaExpression symbol) {
        final String name = symbol.getName();

        return createElement(symbol, name != null ? name : symbol.getText());
    }
    public static LookupElement createSdkElement(LuaDeclarationExpression symbol, Sdk sdk) {
        final String name = symbol.getDefinedName();
        return LookupElementBuilder.create(symbol, name).setTypeText(sdk.getName(), true);
    }

    static final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(LuaFileType.LUA_LANGUAGE);

    public static LookupElement create_GPrefixedElement(LuaDeclarationExpression symbol) {
        String name =  StringUtil.notNullize(symbol.getDefinedName(), symbol.getText());

        if (namesValidator.isIdentifier(name, symbol.getProject()))
            name = "_G.";
        else 
            name = "_G[\"" + name + "\"]";

        return createElement(symbol, name);
    }

    public static LookupElement createStringMetacallElement(String prefix, LuaStringLiteralExpressionImpl literal,
                                                            LuaDeclarationExpression symbol) {
        final String lookupString = "(" + literal.getText() + "):" + symbol.getName();

        return new StringMetaCallLookup(symbol.getName(), lookupString, literal.getTextOffset());
    }

//    public static LookupElement createStringMetacallElement(String prefix, LuaDeclarationExpression symbol) {
//        return new StringMetaCallLookup(prefix, symbol.getName());
//    }

    public static LookupElement createElement(LuaExpression symbol, String name) {
        final Project project = symbol.getProject();
        ProjectRootManager manager = ProjectRootManager.getInstance(project);
        VirtualFile file = symbol.getContainingFile().getVirtualFile();

        if (file != null && !manager.getFileIndex().isInContent(file))
            if (manager.getFileIndex().isInLibraryClasses(file)) {
                final List<OrderEntry> entries = manager.getFileIndex().getOrderEntriesForFile(file);


                OrderEntry first = entries.get(0);


                String libraryName = null;
                if (first instanceof JdkOrderEntry)
                   libraryName =  ((JdkOrderEntry) first).getJdkName();

                if (first instanceof LibraryOrderEntry)
                    libraryName = ((LibraryOrderEntry) first).getLibraryName() ;


                if (libraryName != null)
                    return LookupElementBuilder.create(symbol, name).setTypeText(
                        String.format("< %s > (%s)", libraryName, file.getName()), true).setIcon(LuaIcons.LUA_ICON)
                                                                    .setInsertHandler(new LuaInsertHandler());
            } else {
                return LookupElementBuilder.create(symbol, name).setTypeText("External File", true);
            }

        return LookupElementBuilder.create(symbol, name).setTypeText(symbol.getContainingFile().getName(), true)
                                   .setIcon(LuaIcons.LUA_ICON).setInsertHandler(new LuaInsertHandler());
    }

    public static LookupElement createElement(String s) {
        return LookupElementBuilder.create(s);
    }

    public static LookupElement createKeywordElement(String s) {
        return LookupElementBuilder.create(s).setBold().setIcon(LuaIcons.LUA_ICON);
    }

    public static LookupElement createTypedElement(String s) {
        return new LuaLookupElement(s, true);
    }
    public static LookupElement createTypedElement(LuaDeclarationExpression s) {
        return new LuaLookupElement(s, true);
    }
    public boolean isTypeInfered() {
        return typeInfered;
    }

    static class StringMetaCallLookup extends LuaLookupElement {

        String presentable = null;
        private final int offset;

        public StringMetaCallLookup(String str, String present, int offser) {
            super(str, true);
            presentable = present;
            this.offset = offser;
        }

//        @Override
//        public void renderElement(LookupElementPresentation presentation) {
//            presentation.setItemText(str);
//        }
        @Override
        public void handleInsert(InsertionContext context) {
//            int offset = context.getStartOffset();
            context.getDocument().deleteString(offset, context.getTailOffset());
            context.getDocument().insertString(offset, presentable);
            ParenthesesInsertHandler.WITH_PARAMETERS.handleInsert(context, this);
        }

    }


}



