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

package com.sylvanaar.idea.Lua.lang.psi.util;


import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.sylvanaar.idea.Lua.LuaFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 16.04.2009
 * Time: 16:48:49
 *
 * @author Joachim Ansorg
 */
public class LuaChangeUtil {
    private static final String TEMP_FILE_NAME = "__.sh";

    /*public static void replaceText(final PsiFile file, final TextRange range, final String replacement) {
        final Document document = file.getViewProvider().getDocument();
        assert document != null;

        document.replaceString(range.getStartOffset(), range.getEndOffset(), replacement);
        PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);
        PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(document);
    }

    public static void replaceText(PsiElement element, String replacement) {
        replaceText(element.getContainingFile(), element.getTextRange(), replacement);
    }

    public static void replaceText(PsiElement element, String replacement, TextRange rangeWithinElement) {
        TextRange range = TextRange.from(element.getTextOffset() + rangeWithinElement.getStartOffset(), rangeWithinElement.getLength());
        replaceText(element.getContainingFile(), range, replacement);
    } */


    @NotNull
    private static PsiFile createFileFromText(@NotNull final Project project, @NotNull final String name, @NotNull final FileType fileType, @NotNull final String text) {
        return PsiFileFactory.getInstance(project).createFileFromText(name, fileType, text);
    }

    public static PsiFile createDummyLuaFile(Project project, String text) {
        return createFileFromText(project, TEMP_FILE_NAME, LuaFileType.LUA_FILE_TYPE, text);
    }


    
    public static PsiElement createSymbol(Project project, String name) {
        final PsiElement functionElement = createDummyLuaFile(project, name + "() { x; }");
        return functionElement.getFirstChild().getFirstChild();
    }

    public static PsiElement createWord(Project project, String name) {
        return createDummyLuaFile(project, name).getFirstChild();
    }

    public static PsiElement createAssignmentWord(Project project, String name) {
        final PsiElement assignmentCommand = createDummyLuaFile(project, name + "=a").getFirstChild();

        return assignmentCommand.getFirstChild().getFirstChild();
    }

    public static PsiElement createVariable(Project project, String name, boolean withBraces) {
        if (withBraces) {
            String text = "${" + name + "}";
            PsiElement command = createDummyLuaFile(project, text).getFirstChild();

            //fixme terrible code
            return command.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getFirstChild().getNextSibling();
        }

        String text = "$" + name;
        PsiElement command = createDummyLuaFile(project, text).getFirstChild();

        return command.getFirstChild().getFirstChild();
    }

    public static PsiElement createShebang(Project project, String command, boolean addNewline) {
        String text = "#!" + command + (addNewline ? "\n" : "");
        return createDummyLuaFile(project, text).getFirstChild();
    }
}
