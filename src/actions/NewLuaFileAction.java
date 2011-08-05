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

package com.sylvanaar.idea.Lua.actions;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.LuaBundle;
import com.sylvanaar.idea.Lua.LuaIcons;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 17.04.2009
 * Time: 20:19:17
 *
 * @author Joachim Ansorg
 */
public class NewLuaFileAction extends NewLuaActionBase {
    private static final Logger log = Logger.getInstance("#NewActionBase");

    public NewLuaFileAction() {
        super(LuaBundle.message("newfile.menu.action.text"),
                LuaBundle.message("newfile.menu.action.description"),
                LuaIcons.LUA_ICON);
    }


    protected String getDialogPrompt() {
        return LuaBundle.message("newfile.dialog.prompt");
    }

    protected String getDialogTitle() {
        return LuaBundle.message("newfile.dialog.title");
    }

    protected String getCommandName() {
        return LuaBundle.message("newfile.command.name");
    }

    protected String getActionName(PsiDirectory directory, String newName) {
        return LuaBundle.message("newfile.menu.action.text");
    }

    @NotNull
    protected PsiElement[] doCreate(String newName, PsiDirectory directory) {
        PsiFile file = createFileFromTemplate(directory, newName, LuaTemplatesFactory.NEW_SCRIPT_FILE_NAME);
        PsiElement child = file.getLastChild();
        return child != null ? new PsiElement[]{file, child} : new PsiElement[]{file};
    }
}