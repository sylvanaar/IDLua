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

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.LuaFileType;
import org.jetbrains.annotations.NonNls;

import java.util.Properties;

public class LuaTemplatesFactory  {
    public static final String NEW_SCRIPT_FILE_NAME = "Lua Script.lua";
    public static final String LUA_HEADER_NAME = "Lua File Header.lua";

//    private final FileTemplateGroupDescriptor templateGroup;
    private static final Logger log = Logger.getInstance("Lua.TemplatesFactory");

//    public LuaTemplatesFactory() {
//        templateGroup =
//                new FileTemplateGroupDescriptor(LuaBundle.message("file.template.group.title.lua"), LuaIcons.LUA_ICON);
//        templateGroup.addTemplate(NEW_SCRIPT_FILE_NAME);
//        templateGroup.addTemplate(LUA_HEADER_NAME);
//    }
//
//    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
//        return templateGroup;
//    }

    public static PsiFile createFromTemplate(final PsiDirectory directory, final String name,
                                             String fileName, String templateName,
                                           @NonNls String... parameters) throws IncorrectOperationException {
        log.debug("createFromTemplate: dir:" + directory + ", filename: " + fileName);

        final FileTemplate template = FileTemplateManager.getInstance().getTemplate(templateName);

        Properties properties = new Properties(FileTemplateManager.getInstance().getDefaultProperties());

        String text;

        try {
            text = template.getText(properties);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load template for " +
                                       FileTemplateManager.getInstance().internalTemplateToSubject(templateName), e);
        }

        final PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());

        log.debug("Create file from text");
        final PsiFile file = factory.createFileFromText(fileName, LuaFileType.LUA_FILE_TYPE, text);

        log.debug("Adding file to directory");
        return (PsiFile) directory.add(file);
    }

}