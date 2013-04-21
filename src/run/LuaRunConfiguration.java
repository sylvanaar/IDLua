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
package com.sylvanaar.idea.Lua.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.sylvanaar.idea.Lua.run.kahlua.KahluaCommandLineState;
import com.sylvanaar.idea.Lua.run.lua.LuaCommandLineState;
import com.sylvanaar.idea.Lua.run.luaj.LuaJExternalCommandLineState;
import com.sylvanaar.idea.Lua.sdk.KahluaSdk;
import com.sylvanaar.idea.Lua.sdk.LuaJSdk;
import com.sylvanaar.idea.Lua.sdk.LuaSdkType;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LuaRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> implements CommonLuaRunConfigurationParams, LuaRunConfigurationParams {
    static Logger log = Logger.getLogger(LuaRunConfiguration.class);

    // common config
    private String interpreterOptions = "";
    private String workingDirectory = "";
    private boolean passParentEnvs = true;
    private Map<String, String> envs = new HashMap<String, String>();

    private boolean overrideSDK = false;
    private String interpreterPath = "";

    // run config
    private String scriptName;
    private String scriptParameters;
//    private boolean usingKahlua;
//    private boolean usingLuaJ;

    public LuaRunConfiguration(RunConfigurationModule runConfigurationModule, ConfigurationFactory configurationFactory, String name) {
        super(name, runConfigurationModule, configurationFactory);
    }

    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new LuaRunConfigurationEditor(this);
    }

    @Override
    public boolean excludeCompileBeforeLaunchOption() {
        return true;
    }

    public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
        LuaCommandLineState state = null;

        Sdk sdk = getSdk();
        final boolean isDebugger = executor.getId().equals(DefaultDebugExecutor.EXECUTOR_ID);

        if(sdk != null && sdk.getSdkType() instanceof LuaSdkType) {
            if (sdk.getName().equals(KahluaSdk.NAME)) {
                state = new KahluaCommandLineState(this, env);
            }
            else if (sdk.getName().equals(LuaJSdk.NAME)) {
                state = new LuaJExternalCommandLineState(this, env);
            }

            if ((state != null) && isDebugger)
                throw new ExecutionException("Debugging not supported for SDK " + sdk.getName() + ". Please configure a real Lua SDK.");
        }

        if (state == null) {
            if (isDebugger)
                state = new LuaDebugCommandlineState(this, env);
            else
                state = new LuaCommandLineState(this, env);
        }

        TextConsoleBuilder textConsoleBuilder = new LuaTextConsoleBuilder(getProject());
        textConsoleBuilder.addFilter(new LuaLineErrorFilter(getProject()));

        state.setConsoleBuilder(textConsoleBuilder);
        return state;
    }

    public Sdk getSdk() {return LuaSdkType.findLuaSdk(getConfigurationModule().getModule());}

    public static void copyParams(CommonLuaRunConfigurationParams from, CommonLuaRunConfigurationParams to) {
        to.setEnvs(new HashMap<String, String>(from.getEnvs()));
        to.setInterpreterOptions(from.getInterpreterOptions());
        to.setWorkingDirectory(from.getWorkingDirectory());
        to.setInterpreterPath(from.getInterpreterPath());
        to.setOverrideSDKInterpreter(from.isOverrideSDKInterpreter());
//        to.setUsingLuaJInterpreter(from.isUsingLuaJInterpreter());
        //to.setPassParentEnvs(from.isPassParentEnvs());
    }

    public static void copyParams(LuaRunConfigurationParams from, LuaRunConfigurationParams to) {
        copyParams(from.getCommonParams(), to.getCommonParams());

        to.setScriptName(from.getScriptName());
        to.setScriptParameters(from.getScriptParameters());
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);

        // common config
        interpreterOptions = JDOMExternalizerUtil.readField(element, "INTERPRETER_OPTIONS");
        interpreterPath = JDOMExternalizerUtil.readField(element, "INTERPRETER_PATH");
        workingDirectory = JDOMExternalizerUtil.readField(element, "WORKING_DIRECTORY", getProject().getBasePath());
        
        String str = JDOMExternalizerUtil.readField(element, "PARENT_ENVS");
        if (str != null) {
            passParentEnvs = Boolean.parseBoolean(str);
        }
        str = JDOMExternalizerUtil.readField(element, "ALTERNATE_INTERPRETER");
        if (str != null) {
            overrideSDK = Boolean.parseBoolean(str);
        }
        EnvironmentVariablesComponent.readExternal(element, envs);

        // ???
        getConfigurationModule().readExternal(element);

        // run config
        scriptName = JDOMExternalizerUtil.readField(element, "SCRIPT_NAME");
        scriptParameters = JDOMExternalizerUtil.readField(element, "PARAMETERS");
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        // common config
        JDOMExternalizerUtil.writeField(element, "INTERPRETER_OPTIONS", interpreterOptions);
        JDOMExternalizerUtil.writeField(element, "INTERPRETER_PATH", interpreterPath);
        JDOMExternalizerUtil.writeField(element, "WORKING_DIRECTORY", workingDirectory);
        JDOMExternalizerUtil.writeField(element, "PARENT_ENVS", Boolean.toString(passParentEnvs));
        JDOMExternalizerUtil.writeField(element, "ALTERNATE_INTERPRETER", Boolean.toString(overrideSDK));

        EnvironmentVariablesComponent.writeExternal(element, envs);

        // ???
        getConfigurationModule().writeExternal(element);

        // run config
        JDOMExternalizerUtil.writeField(element, "SCRIPT_NAME", scriptName);
        JDOMExternalizerUtil.writeField(element, "PARAMETERS", scriptParameters);
    }


    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkConfiguration();

        if (overrideSDK) {
            if (StringUtil.isEmptyOrSpaces(interpreterPath)) {
                throw new RuntimeConfigurationException("No interpreter path given.");
            }

            File interpreterFile = new File(interpreterPath);
            if (!interpreterFile.isFile() || !interpreterFile.canRead()) {
                throw new RuntimeConfigurationException("Interpreter path is invalid or not readable.");
            }
        }
        if (StringUtil.isEmptyOrSpaces(scriptName)) {
            throw new RuntimeConfigurationException("No script name given.");
        }

        String name = getScriptName();
        final String dir = getWorkingDirectory();
        if (StringUtil.isNotEmpty(dir))
            name = dir + '/' + name;
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(name);

        if (file == null) throw new RuntimeConfigurationException("Script file does not exist");

        final ProjectRootManager rootManager = ProjectRootManager.getInstance(getProject());

        if (!rootManager.getFileIndex().isInContent(file))
            throw new RuntimeConfigurationException("File is not in the current project");

    }

    public String getInterpreterOptions() {
        return interpreterOptions;
    }

    public void setInterpreterOptions(String interpreterOptions) {
        this.interpreterOptions = interpreterOptions;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public boolean isPassParentEnvs() {
        return passParentEnvs;
    }

    public void setPassParentEnvs(boolean passParentEnvs) {
        this.passParentEnvs = passParentEnvs;
    }

    public Map<String, String> getEnvs() {
        return envs;
    }

    public void setEnvs(Map<String, String> envs) {
        this.envs = envs;
    }

    public String getInterpreterPath() {
        return interpreterPath;
    }

    public void setInterpreterPath(String path) {
        this.interpreterPath = path;
    }

    public CommonLuaRunConfigurationParams getCommonParams() {
        return this;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptParameters() {
        return scriptParameters;
    }

    public void setScriptParameters(String scriptParameters) {
        this.scriptParameters = scriptParameters;
    }

    @Override
    public boolean isOverrideSDKInterpreter() {
        return this.overrideSDK;
    }

    @Override
    public void setOverrideSDKInterpreter(boolean b) {
        this.overrideSDK = b;
    }

    @Override
    public Collection<Module> getValidModules() {
        Module[] allModules = ModuleManager.getInstance(getProject()).getModules();
        return Arrays.asList(allModules);
    }

    @Override
    protected ModuleBasedConfiguration createInstance() {
        return new LuaRunConfiguration(getConfigurationModule(), getFactory(), getName());
    }


}