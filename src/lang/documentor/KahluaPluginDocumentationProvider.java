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

package com.sylvanaar.idea.Lua.lang.documentor;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFieldIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import org.jetbrains.annotations.Nullable;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/20/11
 * Time: 3:06 PM
 */
public class KahluaPluginDocumentationProvider implements DocumentationProvider {
    private static final KahluaConverterManager converterManager = new KahluaConverterManager();
    private static final J2SEPlatform platform = new J2SEPlatform();
    private static final KahluaTable env = platform.newEnvironment();
    private static final KahluaThread thread = new KahluaThread(platform, env);
    private static final LuaCaller caller = new LuaCaller(converterManager);
    private static final LuaJavaClassExposer exposer = new LuaJavaClassExposer(converterManager, platform, env);

    private static final Logger log = Logger.getInstance("#Lua.documenter.KahluaPluginDocumentationProvider");
    private static final String DOC_FILE_SUFFIX = ".doclua";

    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return null;
    }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
       String s =  runLuaDocumentationUrlGenerator(getVirtualFileForElement(element), element.getText());

       if (s == null) return null;

       List<String> rc =  new ArrayList<String>();
       rc.add(s);

       return rc;        
    }

    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {
        return runLuaDocumentationGenerator(getVirtualFileForElement(element), element.getText());
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null;
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null;
    }

    @Nullable
    private VirtualFile getVirtualFileForElement(PsiElement e) {
        if (e instanceof LuaFieldIdentifier) {
            e = ((LuaFieldIdentifier) e).getEnclosingIdentifier();

            assert e instanceof LuaCompoundIdentifier;
        }

        while (e instanceof LuaCompoundIdentifier) {
            e = e.getParent();
        }

        if (e instanceof LuaReferenceElement) {
            PsiElement r = ((LuaReferenceElement) e).resolve();

            if (r != null) {
                VirtualFile vf = r.getContainingFile().getVirtualFile();
                String docFileName = vf.getNameWithoutExtension() + DOC_FILE_SUFFIX;
                return vf.getParent().findChild(docFileName);
            }
        }

        return null;
    }


    
    @Nullable
    private String runLuaQuickNavigateDocGenerator(@Nullable VirtualFile luaFile, String nameToDocument) {
        if (luaFile == null) return null;

        LuaClosure closure = null;
        try {
            exposer.exposeGlobalFunctions(this);

            closure = LuaCompiler.loadis(new FileInputStream(luaFile.getPath()), luaFile.getName(), env);
            caller.protectedCall(thread, closure);

            closure = LuaCompiler.loadstring("return getQuickNavigateDocumentation('"+nameToDocument+"')", "", env);
            LuaReturn rc = caller.protectedCall(thread, closure);

            if (!rc.isSuccess())
                log.info("Error during lua call: " + rc.getErrorString() + "\r\n\r\n" + rc.getLuaStackTrace());

            if (!rc.isEmpty())
                return (String) rc.getFirst();

        } catch (IOException e) {
            log.info("Error in lua documenter", e);
        }

        return null;
    }


    @Nullable
    private String runLuaDocumentationUrlGenerator(@Nullable VirtualFile luaFile, String nameToDocument) {
        if (luaFile == null) return null;
        
        LuaClosure closure = null;
        try {
            exposer.exposeGlobalFunctions(this);

            closure = LuaCompiler.loadis(new FileInputStream(luaFile.getPath()), luaFile.getName(), env);
            caller.protectedCall(thread, closure);

            closure = LuaCompiler.loadstring("return getDocumentationUrl('"+nameToDocument+"')", "", env);
            LuaReturn rc = caller.protectedCall(thread, closure);

            if (!rc.isSuccess())
                log.info("Error during lua call: " + rc.getErrorString() + "\r\n\r\n" + rc.getLuaStackTrace());

            if (!rc.isEmpty())
                return (String) rc.getFirst();
            
        } catch (IOException e) {
            log.info("Error in lua documenter", e);
        }

        return null;
    }


    @Nullable
    private String runLuaDocumentationGenerator(@Nullable VirtualFile luaFile, String nameToDocument) {
        if (luaFile == null) return null;

        LuaClosure closure = null;
        try {
            exposer.exposeGlobalFunctions(this);

            closure = LuaCompiler.loadis(new FileInputStream(luaFile.getPath()), luaFile.getName(), env);
            caller.protectedCall(thread, closure);

            closure = LuaCompiler.loadstring("return getDocumentation('"+nameToDocument+"')", "", env);
            LuaReturn rc = caller.protectedCall(thread, closure);

            if (!rc.isSuccess())
                log.info("Error during lua call: " + rc.getErrorString() + "\r\n\r\n" + rc.getLuaStackTrace());

            if (!rc.isEmpty())
                return (String) rc.getFirst();

        } catch (IOException e) {
            log.info("Error in lua documenter", e);
        }

        return null;
    }
    
}
