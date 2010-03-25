/*
 * Copyright 2009 Max Ishchenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 14.07.2009
 * Time: 19:16:26
 */
@SuppressWarnings({"deprecation"})
public class LuaRunConfiguration extends RuntimeConfiguration implements ModuleRunConfiguration {

    public String serverDescriptorId;
    public boolean showHttpLog;
    public boolean showErrorLog;
    public String httpLogPath;
    public String errorLogPath;

    public LuaRunConfiguration(Project project, ConfigurationFactory LuaConfigurationFactory, String name) {
        super(name, project, LuaConfigurationFactory);
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {

       return null; // LuaRunProfileState(env, getProject());

    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {

//        LuaServersConfiguration config = LuaServersConfiguration.getInstance();
//        LuaServerDescriptor descriptor = config.getDescriptorById(serverDescriptorId);
//        if (descriptor == null) {
//            throw new RuntimeConfigurationException(LuaBundle.message("run.error.noserver"));
//        }


        if (showHttpLog) {
            File accessLogFile = new File(httpLogPath);
            if (accessLogFile.isDirectory()) {
                throw new RuntimeConfigurationException("accesslog is directory");
            }
        }

        if (showErrorLog) {
            File errorLogFile = new File(errorLogPath);
            if (errorLogFile.isDirectory()) {
                throw new RuntimeConfigurationException("errorlog is directory");
            }
        }


//        VirtualFile vfile = LocalFileSystem.getInstance().findFileByPath(descriptor.getExecutablePath());
//        if (vfile == null) {
//            throw new RuntimeConfigurationException(LuaBundle.message("run.error.badpath"));
//        } else {

//            PlatformDependentTools pdt = ApplicationManager.getApplication().getComponent(PlatformDependentTools.class);
//            if (!pdt.checkExecutable(vfile)) {
//                throw new RuntimeConfigurationException(LuaBundle.message("run.error.notexecutable"));
//            }

       // }

    }

    @Override
    public void createAdditionalTabComponents(AdditionalTabComponentManager manager, final ProcessHandler startedProcess) {

        if (showHttpLog) {

            final LuaLogTab httpLogTab = new LuaLogTab(getProject(), new File(httpLogPath));

            manager.addAdditionalTabComponent(httpLogTab, "errorlogtab");
            startedProcess.addProcessListener(new ProcessAdapter() {
                @Override
                public void startNotified(ProcessEvent event) {
                    httpLogTab.poke();
                }

                @Override
                public void processTerminated(ProcessEvent event) {
                    httpLogTab.poke();
                    startedProcess.removeProcessListener(this);
                }
            });

        }

        if (showErrorLog) {

            final LuaLogTab errorLogTab = new LuaLogTab(getProject(), new File(errorLogPath));

            manager.addAdditionalTabComponent(errorLogTab, "accesslogtab");
            startedProcess.addProcessListener(new ProcessAdapter() {
                @Override
                public void startNotified(ProcessEvent event) {
                    errorLogTab.poke();
                }

                @Override
                public void processTerminated(ProcessEvent event) {
                    errorLogTab.poke();
                    startedProcess.removeProcessListener(this);
                }
            });

        }

    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
        super.readExternal(element);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
        super.writeExternal(element);
    }

    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return null; //new LuaRunSettingsEditor(this);
    }

    public String getServerDescriptorId() {
        return serverDescriptorId;
    }

    public void setServerDescriptorId(String serverDescriptorId) {
        this.serverDescriptorId = serverDescriptorId;
    }

    public boolean isShowHttpLog() {
        return showHttpLog;
    }

    public void setShowHttpLog(boolean showHttpLog) {
        this.showHttpLog = showHttpLog;
    }

    public boolean isShowErrorLog() {
        return showErrorLog;
    }

    public void setShowErrorLog(boolean showErrorLog) {
        this.showErrorLog = showErrorLog;
    }

    public String getHttpLogPath() {
        return httpLogPath;
    }

    public void setHttpLogPath(String httpLogPath) {
        this.httpLogPath = httpLogPath;
    }

    public String getErrorLogPath() {
        return errorLogPath;
    }

    public void setErrorLogPath(String errorLogPath) {
        this.errorLogPath = errorLogPath;
    }
}
