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

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.LuaBundle;
import com.sylvanaar.idea.Lua.LuaFileType;
import com.sylvanaar.idea.Lua.LuaIcons;

/**
 * This class acts as a factory for file templates.
 * <p/>
 * Date: 17.04.2009
 * Time: 20:14:23
 *
 * @author Joachim Ansorg
 */
public class LuaTemplatesFactory implements FileTemplateGroupDescriptorFactory {
    private static final String FILE_NAME = "lua-script.lua";
    private final FileTemplateGroupDescriptor templateGroup;
    private static final Logger log = Logger.getInstance("#LuaTemplatesFactory");

    public LuaTemplatesFactory() {
        templateGroup = new FileTemplateGroupDescriptor(LuaBundle.message("file.template.group.title.bash"), LuaIcons.LUA_ICON);
        templateGroup.addTemplate(FILE_NAME);
    }

    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        return templateGroup;
    }

    public static PsiFile createFromTemplate(final PsiDirectory directory, final String name, String fileName) throws IncorrectOperationException {
        log.debug("createFromTemplate: dir:" + directory + ", filename: " + fileName);

        final String text = "";
        final PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());

        log.debug("Create file from text");
        final PsiFile file = factory.createFileFromText(fileName, LuaFileType.LUA_FILE_TYPE, text);

        log.debug("Adding file to directory");
        return (PsiFile) directory.add(file);
    }

}