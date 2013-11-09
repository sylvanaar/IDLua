/*
 * Copyright 2013 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.extensions;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.sylvanaar.idea.Lua.editor.inspections.AbstractInspection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LuaInspectionProvider {
    private static final Logger log = Logger.getInstance("Lua.extensions.LuaInspectionProvider");
    private static final ExtensionPointName<LuaInspectionProvider> EP_NAME = ExtensionPointName.create("com.sylvanaar.idea.Lua.extensions.inspectionProvider");
    private final Object VMLock = new Object();

    private static final J2SEPlatform platform = new J2SEPlatform();
    private static final KahluaConverterManager converterManager = new KahluaConverterManager();
    private static final LuaCaller caller = new LuaCaller(converterManager);
    private static final Map<VirtualFile, ScriptEnvironment> scriptEnvironmentMap =
            new HashMap<VirtualFile, ScriptEnvironment>();
    private final MyAbstractInspection abstractInspection = new MyAbstractInspection();

    @NotNull
    public String[] getGroupPath() {
        return abstractInspection.getGroupPath();
    }

    @NotNull
    public String getShortName() {
        return abstractInspection.getShortName();
    }

    public boolean isEnabledByDefault() {
        return abstractInspection.isEnabledByDefault();
    }

    @NotNull
    public HighlightDisplayLevel getDefaultLevel() {
        return abstractInspection.getDefaultLevel();
    }

    public boolean isSuppressedFor(PsiElement element) {
        return abstractInspection.isSuppressedFor(element);
    }

    public SuppressIntentionAction[] getSuppressActions(@Nullable PsiElement element) {
        return abstractInspection.getSuppressActions(element);
    }

    @NotNull
    public String getGroupDisplayName() {
        return abstractInspection.getGroupDisplayName();
    }

    private static class ScriptEnvironment {
        KahluaTable env = platform.newEnvironment();
        final KahluaThread thread = new KahluaThread(platform, env);
        final LuaJavaClassExposer exposer = new LuaJavaClassExposer(converterManager, platform, env);
    }

    private static final ScriptEnvironment SHARED_SCRIPT_ENV = new ScriptEnvironment();

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
    private String runLua(String function, ScriptEnvironment scriptEnvironment) {
        if (StringUtil.isEmpty(function)) return null;
        if (scriptEnvironment == null) return null;

        synchronized (VMLock) {
            try {
                LuaClosure closure = LuaCompiler.loadstring(
                        "return " + function + "()", "", scriptEnvironment.env
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

    private class MyAbstractInspection extends AbstractInspection {
        @Nullable
        @Override
        public String getStaticDescription() {
            return runLua("getStaticDescription", SHARED_SCRIPT_ENV);
        }

        @Nls
        @NotNull
        @Override
        public String getDisplayName() {
            final String displayName = runLua("getDisplayName", SHARED_SCRIPT_ENV);

            assert displayName != null : "Display name cannot be null";

            return displayName;

        }

        @Nls
        @NotNull
        @Override
        public String getGroupDisplayName() {
            final String groupDisplayName = runLua("getGroupDisplayName", SHARED_SCRIPT_ENV);

            assert groupDisplayName != null : "Group display name cannot be null";

            return groupDisplayName;
        }

        @NotNull
        @Override
        public HighlightDisplayLevel getDefaultLevel() {
            final String displayLevel = runLua("getDefaultLevel", SHARED_SCRIPT_ENV);

            assert displayLevel != null : "Group display name cannot be null";

            if (displayLevel.equals("warning"))
                return HighlightDisplayLevel.WARNING;

            return HighlightDisplayLevel.GENERIC_SERVER_ERROR_OR_WARNING;
        }

        @NotNull
        @Override
        public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
            return super.buildVisitor(holder, isOnTheFly); // Call into Lua
        }
    }
}
