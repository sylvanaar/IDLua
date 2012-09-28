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
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PathUtil;
import com.sylvanaar.idea.Lua.lang.psi.LuaNamedElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaAlias;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobal;
import com.sylvanaar.idea.Lua.util.UrlUtil;
import org.jetbrains.annotations.Nullable;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/20/11
 * Time: 3:06 PM
 */
public class KahluaPluginDocumentationProvider implements DocumentationProvider {
    private static final KahluaConverterManager converterManager = new KahluaConverterManager();
    private static final J2SEPlatform platform = new J2SEPlatform();

    private static final LuaCaller caller = new LuaCaller(converterManager);


    private final Object VMLock = new Object();

    private static final Logger log = Logger.getInstance("Lua.documenter.KahluaPluginDocumentationProvider");
    private static final String DOC_FILE_SUFFIX = ".doclua";

    private static final Map<VirtualFile, ScriptEnvironment> scriptEnvironmentMap =
            new HashMap<VirtualFile, ScriptEnvironment>();


    private class ScriptEnvironment {
        KahluaTable env = platform.newEnvironment();
        final KahluaThread thread = new KahluaThread(platform, env);
        final LuaJavaClassExposer exposer = new LuaJavaClassExposer(converterManager, platform, env);
    }


    @LuaMethod(name="log", global = true)
    public void luaLog(String msg) {
        log.info(msg);
    }

    @LuaMethod(name="disableCache", global = true)
    public void clearCaches() {
        scriptEnvironmentMap.clear();
    }

    @LuaMethod(name="fetchURL", global = true)
    public String fetchURL(String url) {
        UrlUtil.UrlFetcher fetcher = new UrlUtil.UrlFetcher(url);
        fetcher.run();
        return fetcher.getData();
    }

    @LuaMethod(name="getBaseJarUrl", global = true)
    public String getBaseJarUrl() {
        String url = VfsUtil.pathToUrl(PathUtil.getJarPathForClass(LuaPsiFile.class));
        VirtualFile sdkFile = VirtualFileManager.getInstance().findFileByUrl(url);
        if (sdkFile != null) {
            VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(sdkFile);
            if (jarFile != null) {
                return jarFile.getUrl();
            } else {
                return sdkFile.getUrl();
            }
        }

        return null;
    }

    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (element instanceof LuaNamedElement)
            return runLuaQuickNavigateDocGenerator(getVirtualFileForElement(element), getElementName(element));

        return null;
    }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
       String s =  runLuaDocumentationUrlGenerator(getVirtualFileForElement(element), getElementName(element));

       if (s == null) return null;

       List<String> rc =  new ArrayList<String>();
       rc.add(s);

       return rc;
    }

    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {
        log.debug("element = " + element);
        log.debug("originalElement = " + originalElement);

        element = resolveReferencesAndAliases(element);

        log.debug("element = " + element);
        return runLuaDocumentationGenerator(getVirtualFileForElement(element), getElementName(element));
    }

    private PsiElement resolveReferencesAndAliases(PsiElement element) {
        List<PsiElement> processed = new ArrayList<PsiElement>();

        while (!processed.contains(element)) {
           processed.add(element);
           if (element instanceof LuaAlias) {
               PsiElement alias = ((LuaAlias) element).getAliasElement();
               if (alias == null) break;
               element = alias;
           }

           if (element instanceof LuaReferenceElement) {
               PsiElement result = ((LuaReferenceElement) element).resolve();
               if (result == null) break;
               element = result;
           }
       }
        return element;
    }

    private String getElementName(PsiElement element) {
        if (element instanceof LuaGlobal)
            return ((LuaGlobal) element).getGlobalEnvironmentName();

        return element.getText();
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
//        PsiElement r = e;

//        if (e instanceof LuaDeclarationExpression) {
//            r = e;
//        } else {
//            if (e instanceof LuaFunctionCallExpression)
//                e = ((LuaFunctionCallExpression) e).getFunctionNameElement();
//
//            if (e instanceof LuaFieldIdentifier) {
//                e = ((LuaFieldIdentifier) e).getEnclosingIdentifier();
//
//                assert e instanceof LuaCompoundIdentifier;
//            }
//
//            while (e instanceof LuaCompoundIdentifier) {
//                e = e.getParent();
//            }
//
//            if (! (e instanceof LuaReferenceElement))
//                e = e.getParent();
//
//
//            if (e instanceof LuaReferenceElementImpl) {
//                r = ((LuaReferenceElementImpl) e).getResolvedElement();
//            }
//        }

        if (e != null) {
            final PsiFile containingFile = e.getContainingFile();
            if (containingFile == null) return null;

            VirtualFile vf = containingFile.getVirtualFile();

            if (vf != null) {
                return findDocLuaFile(vf);
            }
        }


        return null;
    }

    private VirtualFile findDocLuaFile(VirtualFile vf) {
        String docFileName = vf.getNameWithoutExtension() + DOC_FILE_SUFFIX;

        log.debug("trying file " + docFileName);
        VirtualFile result = vf.getParent().findChild(docFileName);
        if (result != null) return result;

        docFileName = vf.getParent().getNameWithoutExtension() + DOC_FILE_SUFFIX;

        log.debug("trying file " + docFileName);
        result = vf.getParent().findChild(docFileName);

        return result;
    }


    private ScriptEnvironment getScriptEnvironmentForFile(VirtualFile vf) throws IOException {
        synchronized (VMLock) {

        if (scriptEnvironmentMap.containsKey(vf))
            return scriptEnvironmentMap.get(vf);

        ScriptEnvironment scriptEnvironment = new ScriptEnvironment();
        scriptEnvironment.exposer.exposeGlobalFunctions(this);

        // Cache the environment
        scriptEnvironmentMap.put(vf, scriptEnvironment);

        // Run the initial script
        LuaClosure closure = LuaCompiler.loadis(vf.getInputStream(), vf.getName(), scriptEnvironment.env);
        LuaReturn rc = caller.protectedCall(scriptEnvironment.thread, closure);

        if (!rc.isSuccess())
            log.info("Error during initial lua call: " + rc.getErrorString() + "\r\n\r\n" + rc.getLuaStackTrace());

        return scriptEnvironment;
        }
    }


    @Nullable
    private String runLuaQuickNavigateDocGenerator(@Nullable VirtualFile luaFile, String nameToDocument) {
        return runLua("getQuickNavigateDocumentation", luaFile, nameToDocument);
    }


    @Nullable
    private String runLuaDocumentationUrlGenerator(@Nullable VirtualFile luaFile, String nameToDocument) {
        return runLua("getDocumentationUrl", luaFile, nameToDocument);
    }



    @Nullable
    private String runLuaDocumentationGenerator(@Nullable VirtualFile luaFile, String nameToDocument) {
        return runLua("getDocumentation", luaFile, nameToDocument);
    }


    @Nullable
    private String runLua(String function, @Nullable VirtualFile luaFile, String nameToDocument) {
        if (luaFile == null) return null;

       String docLuaFileUrl = luaFile.getParent().getUrl();

        synchronized (VMLock) {
            try {
                ScriptEnvironment scriptEnvironment = getScriptEnvironmentForFile(luaFile);

                if (scriptEnvironment == null) return null;

                LuaClosure closure = LuaCompiler.loadstring(
                        "return " + function + "('" + nameToDocument + "', '" + docLuaFileUrl + "')", "", scriptEnvironment.env
                                                           );
                LuaReturn rc = caller.protectedCall(scriptEnvironment.thread, closure);

                if (!rc.isSuccess())
                    log.info("Error during lua call: " + rc.getErrorString() + "\r\n\r\n" + rc.getLuaStackTrace());

                if (!rc.isEmpty()) {
                    String unencoded = (String) rc.getFirst();

                    byte[] bytes = unencoded.getBytes();

                    return new String(bytes, CharsetToolkit.UTF8);
                }

            } catch (IOException e) {
                log.info("Error in lua documenter", e);
            }
        }
        return null;
    }

}
