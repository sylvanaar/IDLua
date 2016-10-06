/*
 * Copyright 2016 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.options;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.sylvanaar.idea.Lua.util.LuaFileUtil;
import com.sylvanaar.idea.Lua.util.LuaSystemUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Searches platform-specific common paths to find
 * Lua interpreters.
 */
public class LuaInterpreterFinder {

    static final String[] PATHS_UNIX = new String[] {
            "/bin",
            "/sbin",
            "/usr/bin",
            "/usr/sbin",
            "/usr/local/bin",
            "/usr/local/sbin",
            "/opt/bin",
            "/opt/sbin",
            "/opt/local/bin",
            "/opt/local/sbin",
            "${HOME}/bin",
            "${HOME}/sbin",
            "${HOME}/torch/install/bin",
    };

    // TODO: Search Path Globs
    static final String[] PATHS_WINDOWS = new String[] {
            "C:\\Program Files\\Lua 5.1",
            "C:\\Program Files\\Lua 5.2",
            "C:\\Program Files\\Lua 5.3",
            "C:\\Program Files (x86)\\Lua 5.1",
            "C:\\Program Files (x86)\\Lua 5.2",
            "C:\\Program Files (x86)\\Lua 5.3",
    };

    static final Pattern envVarPattern = Pattern.compile(".*\\$\\{([^\\}]+)\\}.*");

    String[] searchPaths;
    VirtualFileManager vfsManager = VirtualFileManager.getInstance();

    public LuaInterpreterFinder() {
        if (SystemInfo.isWindows)
            searchPaths = PATHS_WINDOWS;
        else
            searchPaths = PATHS_UNIX;
    }

    protected VirtualFile getDirectory(String path) {
        VirtualFile virtualFile = vfsManager.findFileByUrl("file://" + path);
        return (virtualFile != null
                && virtualFile.exists()
                && virtualFile.isDirectory())
                ? virtualFile
                : null;
    }

    protected String substituteEnvVars(String into) {
        Matcher m = envVarPattern.matcher(into);
        while (m.matches()) {
            String varName = m.group(1);
            String varValue = System.getenv(varName);
            if (varValue == null)
                varValue = "";
            into = into.replace("${" + varName + "}", varValue);
            m = envVarPattern.matcher(into);
        }
        return into;
    }

    protected ProcessOutput getJarProcessOutput(VirtualFile interpreterExecutable, VirtualFile container) {
        // Convert the virtual file of the container back to a path string
        String workingDirectory = container.getCanonicalPath();
        if (workingDirectory == null)
            return null;

        String exePath = interpreterExecutable.getCanonicalPath();
        if (exePath == null)
            return null;

        // Execute the process and ask for its version number
        ProcessOutput processOutput;
        try {
            processOutput = LuaSystemUtil.getProcessOutput(
                    workingDirectory,
                    "java",
                    "-cp",
                    exePath,
                    "lua",
                    "-v"
            );
        } catch (final ExecutionException e2) {
            return null;
        }

        return processOutput;
    }

    protected ProcessOutput getBinaryProcessOutput(VirtualFile interpreterExecutable, VirtualFile container) {
        // Convert the virtual file of the container back to a path string
        String workingDirectory = container.getCanonicalPath();
        if (workingDirectory == null)
            return null;

        String exePath = interpreterExecutable.getCanonicalPath();
        if (exePath == null)
            return null;

        // Execute the process and ask for its version number
        ProcessOutput processOutput;
        try {
            processOutput = LuaSystemUtil.getProcessOutput(
                    workingDirectory,
                    exePath,
                    "--version"
            );
            if (processOutput.getExitCode() != 0)
                throw new ExecutionException("Invalid parameter");
        } catch (final ExecutionException e1) {
            try {
                processOutput = LuaSystemUtil.getProcessOutput(
                        workingDirectory,
                        exePath,
                        "-v"
                );
            } catch (final ExecutionException e2) {
                return null;
            }
        }

        return processOutput;
    }

    protected void setAsInvalid(LuaInterpreter interpreter) {
        interpreter.setFamily(LuaInterpreterFamily.INVALID_INTERPRETER);
        interpreter.setVersion(null);
    }

    public void describe(LuaInterpreter interpreter) {
        final String exePath = interpreter.path;

        if (exePath == null || exePath.isEmpty() || exePath.trim().isEmpty()) {
            setAsInvalid(interpreter);
            return;
        }

        // Locate the virtual file
        final VirtualFileManager vfsManager = VirtualFileManager.getInstance();
        final VirtualFile interpreterExeFile = vfsManager.findFileByUrl("file://" + exePath.trim());
        if (interpreterExeFile == null) {
            setAsInvalid(interpreter);
            return;
        }

        // Launch the process
        final ProcessOutput processOutput;

        if ("jar".equals(interpreterExeFile.getExtension())) {
            processOutput = getJarProcessOutput(
                    interpreterExeFile,
                    interpreterExeFile.getParent()
            );
        } else {
            processOutput = getBinaryProcessOutput(
                    interpreterExeFile,
                    interpreterExeFile.getParent()
            );
        }

        if (processOutput == null) {
            setAsInvalid(interpreter);
            return;
        }

        String outputText = processOutput.getStderr().trim();
        if (outputText.isEmpty())
            outputText = processOutput.getStdout().trim();

        if (outputText.contains("\n")) {
            String[] lines = outputText.split("\n");
            outputText = lines[0];
        }

        if (outputText.isEmpty()) {
            setAsInvalid(interpreter);
            return;
        }

        // Find the name and version number
        final Pattern versionPattern = Pattern.compile("^(\\S+)\\s+(\\S+).*");
        final Matcher m = versionPattern.matcher(outputText);
        if (!m.matches()) {
            setAsInvalid(interpreter);
            return;
        }

        // Find a matching family
        String familyMatch = m.group(1);
        LuaInterpreterFamily interpreterFamily = LuaInterpreterFamily.findByMatch(
                familyMatch,
                interpreterExeFile.getName()
        );
        if (interpreterFamily == null) {
            setAsInvalid(interpreter);
            return;
        }

        // Success
        interpreter.setFamily(interpreterFamily);
        interpreter.setVersion(m.group(2));
        interpreter.path = exePath.trim();
    }

    protected LuaInterpreter validateInterpreter(VirtualFile executable, LuaInterpreterFamily family) {
        if (executable == null)
            return null;

        LuaInterpreter possibleResult = new LuaInterpreter();
        possibleResult.path = executable.getPath();
        describe(possibleResult);
        if (family.familyNameMatch.equals(possibleResult.family.familyNameMatch)) {
            possibleResult.family = family;
            possibleResult.name = possibleResult.family.interpreterName + " (System)";
            return possibleResult;
        }

        return null;
    }

    protected void findInterpretersInPath(String directoryName, List<LuaInterpreter> results) {
        VirtualFile directory = getDirectory(directoryName);
        if (directory == null)
            return;

        for (LuaInterpreterFamily family : LuaInterpreterFamily.FAMILIES) {
            String exeName = family.getPlatformExecutableName();

            if (LuaFileUtil.isGlob(exeName)) {
                // Match the glob
                Pattern globPattern = LuaFileUtil.patternFromGlob(exeName);
                for (VirtualFile executable : directory.getChildren()) {
                    if (!globPattern.matcher(executable.getName()).matches())
                        continue;

                    LuaInterpreter result = validateInterpreter(executable, family);
                    if (result != null)
                        results.add(result);
                }
            } else {
                VirtualFile executable = directory.findChild(exeName);
                LuaInterpreter result = validateInterpreter(executable, family);
                if (result != null)
                    results.add(result);
            }
        }
    }

    @NotNull
    public List<LuaInterpreter> findInterpreters() {
        List<LuaInterpreter> results = new ArrayList<LuaInterpreter>();
        for (String directoryName : searchPaths) {
            directoryName = substituteEnvVars(directoryName);
            findInterpretersInPath(directoryName, results);
        }

        VirtualFile libDirectory = LuaFileUtil.getPluginVirtualDirectoryChild("lib");
        if (libDirectory != null) {
            String directoryName = libDirectory.getCanonicalPath();
            findInterpretersInPath(directoryName, results);
        }

        return results;
    }

    public static final LuaInterpreterFinder INSTANCE = new LuaInterpreterFinder();
}
