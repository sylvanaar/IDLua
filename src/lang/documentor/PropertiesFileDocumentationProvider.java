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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.sylvanaar.idea.Lua.util.LuaFileUtil;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/11/11
 * Time: 1:40 PM
 */
public class PropertiesFileDocumentationProvider implements DocumentationProvider {

    Map<String, String> CACHE = new Hashtable<String, String>();
    private static final String PROVIDER_NAME = "lua.api.provider";


    void EnumerateProperties(final String key, final Project project) {
        ModuleManager mm = ModuleManager.getInstance(project);
       
        for (final Module module : mm.getModules()) {
            ModuleRootManager mrm = ModuleRootManager.getInstance(module);
            Sdk sdk = mrm.getSdk();

            if (sdk != null) {
                VirtualFile[] vf = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);

                for (VirtualFile libraryFile : vf)
                    LuaFileUtil.iterateRecursively(libraryFile, new ContentIterator() {
                        @Override
                        public boolean processFile(VirtualFile fileOrDir) {
                            if (fileOrDir.getExtension().equals("properties")) {

                                Properties p = new Properties();

                                try {
                                    p.load(fileOrDir.getInputStream());

                                    String result = p.getProperty(key);

                                    if (result!=null) {
                                        String provider = p.getProperty(PROVIDER_NAME);

                                        if (provider != null) {
                                            result = "[" + provider + "]\n " + result;
                                        }

                                        CACHE.put(key, result);
                                        return false;
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }


                            }
                            return true;
                        }
                    });
            }
        }
    }

    @Override
    public String getQuickNavigateInfo(PsiElement psiElement, PsiElement psiElement1) {
        if (psiElement instanceof PsiNamedElement) {
            String name = ((PsiNamedElement) psiElement).getName();

            assert name != null;

            String s = CACHE.get(name);

            if (s == null) {
                EnumerateProperties(name, psiElement.getProject());
                s = CACHE.get(name);
            }
            return s;
        }

        return null;
    }

    @Override
    public List<String> getUrlFor(PsiElement psiElement, PsiElement psiElement1) {
        return null;
    }

    @Override
    public String generateDoc(PsiElement psiElement, PsiElement psiElement1) {
        return null;
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object o, PsiElement psiElement) {
        return null;
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String s, PsiElement psiElement) {
        return null;
    }
}
