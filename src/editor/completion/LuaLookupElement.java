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
    static final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(LuaFileType.LUA_LANGUAGE);
    private String str;
    private boolean typeInferred = false;
    private Object obj;

    public LuaLookupElement(String str) {
        this.str = str;
    }

    public LuaLookupElement(String str, boolean typeInferred) {
        this.str = str;
        this.typeInferred = typeInferred;
    }

    public LuaLookupElement(LuaDeclarationExpression symbol, boolean typeInferred) {
        this.str = StringUtil.notNullize(symbol.getDefinedName(), symbol.getText());
        this.obj = symbol;
        this.typeInferred = typeInferred;
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
        return LookupElementBuilder.create(symbol, name).withTypeText(sdk.getName(), true);
    }

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

    public static LookupElement createNearbyUsageElement(String name) {
        return new FromNearbyUsageLookup(name);
    }

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
                    return LookupElementBuilder.create(symbol, name).withTypeText(
                            String.format("< %s > (%s)", libraryName, file.getName()), true).withIcon(LuaIcons.LUA_ICON)
                                                                    .withInsertHandler(new LuaInsertHandler());
            } else {
                return LookupElementBuilder.create(symbol, name).withTypeText("External File", true);
            }

        return LookupElementBuilder.create(symbol, name).withTypeText(symbol.getContainingFile().getName(), true)
                                   .withIcon(LuaIcons.LUA_ICON).withInsertHandler(new LuaInsertHandler());
    }

    public static LookupElement createElement(String s) {
        return LookupElementBuilder.create(s);
    }

//    public static LookupElement createStringMetacallElement(String prefix, LuaDeclarationExpression symbol) {
//        return new StringMetaCallLookup(prefix, symbol.getName());
//    }

    public static LookupElement createKeywordElement(String s) {
        return LookupElementBuilder.create(s).withBoldness(true).withIcon(LuaIcons.LUA_ICON);
    }

    public static LookupElement createTypedElement(String s) {
        return new LuaLookupElement(s, true);
    }

    public static LookupElement createTypedElement(LuaDeclarationExpression s) {
        return new LuaLookupElement(s, true);
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);
        presentation.setIcon(LuaIcons.LUA_ICON);

        if (typeInferred) {
            presentation.setTypeText("(inferred)");
            presentation.setTypeGrayed(true);
        }

    }

    @NotNull
    @Override
    public Object getObject() {
        if (obj == null) return super.getObject();

        return obj;
    }

    @NotNull
    public String getLookupString() {
        return str;
    }

    public boolean isTypeInferred() {
        return typeInferred;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LuaLookupElement that = (LuaLookupElement) o;

        if (typeInferred != that.typeInferred) return false;
        if (obj != null ? !obj.equals(that.obj) : that.obj != null) return false;
        if (str != null ? !str.equals(that.str) : that.str != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = str != null ? str.hashCode() : 0;
        result = 31 * result + (typeInferred ? 1 : 0);
        result = 31 * result + (obj != null ? obj.hashCode() : 0);
        return result;
    }

    static class StringMetaCallLookup extends LuaLookupElement {

        private final int offset;
        String presentable = null;

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

    static class FromNearbyUsageLookup extends LuaLookupElement {
        public FromNearbyUsageLookup(String name) {
            super(name, false);
        }

        @Override
        public void renderElement(LookupElementPresentation presentation) {
            presentation.setItemText(getLookupString());
            presentation.setTypeText("(from usage)");
            presentation.setTypeGrayed(true);
        }
    }

}



