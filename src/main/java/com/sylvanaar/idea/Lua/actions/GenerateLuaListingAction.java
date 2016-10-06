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

package com.sylvanaar.idea.Lua.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
import com.sylvanaar.idea.Lua.sdk.StdLibrary;
import com.sylvanaar.idea.Lua.util.LuaSystemUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 8/5/11
 * Time: 8:55 AM
 */
public class GenerateLuaListingAction extends AnAction {
    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setVisible(false);
        e.getPresentation().setEnabled(false);
        Project project = e.getData(LangDataKeys.PROJECT);
        if (project != null) {
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                e.getPresentation().setVisible(true);
                Sdk luaSdk = LuaSdkType.findLuaSdk(module);
                if (luaSdk == null) continue;

                final String homePath = luaSdk.getHomePath();
                if (homePath == null) continue;

                if (LuaSdkType.getByteCodeCompilerExecutable(homePath).exists()) {
                    e.getPresentation().setEnabled(true);
                    break;
                }
            }
        }
    }

    PsiElement created;
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(LangDataKeys.PROJECT);
        assert project != null;

        Module module = e.getData(LangDataKeys.MODULE);
        assert module != null;

        Sdk    sdk = LuaSdkType.findLuaSdk(module);
        assert sdk != null;

        final String homePath = sdk.getHomePath();
        if (homePath == null) return;

        String path = LuaSdkType.getByteCodeCompilerExecutable(homePath).getParent();
        String exePath = LuaSdkType.getTopLevelExecutable(homePath).getAbsolutePath();

        PsiFile currfile = e.getData(LangDataKeys.PSI_FILE);
        if (currfile == null || !(currfile instanceof LuaPsiFile)) return;

        FileDocumentManager.getInstance().saveAllDocuments();
        LuaSystemUtil.clearConsoleToolWindow(project);
        
        final VirtualFile virtualFile = currfile.getVirtualFile();
        if (virtualFile == null) return;

        final ProcessOutput processOutput;
        try {
            final VirtualFile child = StdLibrary.getListingModuleLocation().findChild("listing.lua");
            if (child == null) return;

            final String listingScript = child.getPath();
            processOutput = LuaSystemUtil.getProcessOutput(path, exePath, listingScript, virtualFile.getPath());
        } catch (final ExecutionException ex) {
            return;
        }
        if (processOutput.getExitCode() != 0) return;

        String errors = processOutput.getStderr();
        if (StringUtil.notNullize(errors).length() > 0) {
            LuaSystemUtil.printMessageToConsole(project, errors, ConsoleViewContentType.ERROR_OUTPUT);
            return;
        }
        String listing = processOutput.getStdout();

        final IdeView view = LangDataKeys.IDE_VIEW.getData(e.getDataContext());
        if (view == null) return;

        final PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null) return;

        final PsiFileFactory factory = PsiFileFactory.getInstance(project);
        final String listingFileName = virtualFile.getNameWithoutExtension() + "-listing.lua";

        final PsiFile existingFile = dir.findFile(listingFileName);
        if (existingFile != null) ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                existingFile.delete();
            }
        });


        final PsiFile file = factory.createFileFromText(listingFileName, LuaFileType.LUA_FILE_TYPE, listing);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
               created = dir.add(file);
            }
        });

        if (created == null) return;
        final PsiFile containingFile = created.getContainingFile();
        if (containingFile == null) return;
        final VirtualFile virtualFile1 = containingFile.getVirtualFile();
        if (virtualFile1 == null) return;
        
        OpenFileDescriptor fileDesc = new OpenFileDescriptor(project, virtualFile1);

        FileEditorManager.getInstance(project).openTextEditor(fileDesc, false);

        view.selectElement(created);

        created = null;
    }
}
