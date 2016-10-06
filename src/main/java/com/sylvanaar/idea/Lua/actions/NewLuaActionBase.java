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

import com.intellij.CommonBundle;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.LuaFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;


/**
 * Date: 17.04.2009
 * Time: 20:20:20
 *
 * @author Joachim Ansorg
 */
abstract class NewLuaActionBase extends CreateElementActionBase {
    private static final Logger log = Logger.getInstance("#NewActionBase");

    public NewLuaActionBase(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    @NotNull
    protected final PsiElement[] invokeDialog(final Project project, final PsiDirectory directory) {
        log.debug("invokeDialog");
        final MyInputValidator validator = new MyInputValidator(project, directory);
        Messages.showInputDialog(project, getDialogPrompt(), getDialogTitle(), Messages.getQuestionIcon(), "", validator);

        final PsiElement[] elements = validator.getCreatedElements();
        log.debug("Result: " + Arrays.toString(elements));
        return elements;
    }

    public void update(final AnActionEvent event) {
        log.debug("update");
        super.update(event);

        final Presentation presentation = event.getPresentation();
        final DataContext context = event.getDataContext();
        Module module = (Module) context.getData(LangDataKeys.MODULE.getName());

        log.debug("update: module: " + module);

        final boolean hasModule = module != null;
        presentation.setEnabled(hasModule);
        presentation.setVisible(hasModule);
    }

    protected static PsiFile createFileFromTemplate(final PsiDirectory directory,
                                                    String className,
                                                    @NonNls String templateName,
                                                    @NonNls String... parameters) throws IncorrectOperationException {
        log.debug("createFileFromTemplate");
        final String ext = "." + LuaFileType.DEFAULT_EXTENSION;
        String filename = (className.endsWith(ext)) ? className : className + ext;
        return LuaTemplatesFactory.createFromTemplate(directory, className, filename, templateName, parameters);
    }

    @NotNull
    protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception {
        log.debug("create " + newName + ", dir: " + directory);
        return doCreate(newName, directory);
    }

    @NotNull
    protected abstract PsiElement[] doCreate(String newName, PsiDirectory directory);

    protected abstract String getDialogPrompt();

    protected abstract String getDialogTitle();

    protected String getErrorTitle() {
        return CommonBundle.getErrorTitle();
    }

    protected void checkBeforeCreate(String newName, PsiDirectory directory) throws IncorrectOperationException {
        checkCreateFile(directory, newName);
    }

    public static void checkCreateFile(@NotNull PsiDirectory directory, String name) throws IncorrectOperationException {
        final String fileName = name + "." + LuaFileType.DEFAULT_EXTENSION;
        directory.checkCreateFile(fileName);
    }
}