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

import com.intellij.openapi.util.SystemInfo;
import com.sylvanaar.idea.Lua.util.LuaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaInterpreterFamily {

    public enum BinaryType {
        SystemBinary,
        JavaJar,
    }

    @NotNull
    String interpreterName;
    @NotNull
    String executableName;
    @NotNull
    String familyNameMatch;
    @NotNull
    BinaryType binaryType;
    @Nullable
    String argExecCode;
    @Nullable
    String argLoadLib;

    public LuaInterpreterFamily(@NotNull String interpreterName,
                                @NotNull String executableName,
                                @NotNull String familyNameMatch,
                                @NotNull BinaryType binaryType,
                                @Nullable String argExecCode,
                                @Nullable String argLoadLib) {
        this.interpreterName = interpreterName;
        this.executableName = executableName;
        this.familyNameMatch = familyNameMatch;
        this.binaryType = binaryType;
        this.argExecCode = argExecCode;
        this.argLoadLib = argLoadLib;
    }

    public LuaInterpreterFamily(@NotNull String interpreterName,
                                @NotNull String executableName,
                                @NotNull String familyNameMatch,
                                @NotNull BinaryType binaryType) {
        this.interpreterName = interpreterName;
        this.executableName = executableName;
        this.familyNameMatch = familyNameMatch;
        this.binaryType = binaryType;
    }

    @NotNull
    public String getPlatformExecutableName() {
        if (binaryType == BinaryType.JavaJar
                || !SystemInfo.isWindows)
            return executableName;

        return executableName + ".exe";
    }

    public boolean hasLoadLib() {
        return (argLoadLib != null);
    }

    public boolean hasExecCode() {
        return (argExecCode != null);
    }

    public static final LuaInterpreterFamily[] FAMILIES = {
            new LuaInterpreterFamily("Lua", "lua", "Lua", BinaryType.SystemBinary, "-e", "-l"),
            new LuaInterpreterFamily("LuaJIT", "luajit", "LuaJIT", BinaryType.SystemBinary, "-e", "-l"),
            new LuaInterpreterFamily("Tarantool", "tarantool", "Tarantool", BinaryType.SystemBinary),
            new LuaInterpreterFamily("LOVE", "love", "LOVE", BinaryType.SystemBinary),
            new LuaInterpreterFamily("Torch", "qlua", "Lua", BinaryType.SystemBinary, "-e", "-l"),
            new LuaInterpreterFamily("LuaJ JSE", "luaj-jse*.jar", "Luaj-jse", BinaryType.JavaJar),
            new LuaInterpreterFamily("LuaJ JME", "luaj-jme*.jar", "Luaj-jme", BinaryType.JavaJar),
    };

    public static final LuaInterpreterFamily INVALID_INTERPRETER = new LuaInterpreterFamily("Invalid", "", "", BinaryType.SystemBinary);
    public static final LuaInterpreterFamily UNKNOWN_INTERPRETER = new LuaInterpreterFamily("Unknown", "", "", BinaryType.SystemBinary);

    public static LuaInterpreterFamily findByName(@NotNull String s) {
        for (LuaInterpreterFamily type : FAMILIES)
            if (type.interpreterName.equals(s))
                return type;
        return null;
    }

    public static LuaInterpreterFamily findByMatch(@NotNull String s, @NotNull String e) {
        for (LuaInterpreterFamily family : FAMILIES) {
            if (!family.familyNameMatch.equals(s))
                continue;

            if (LuaFileUtil.isGlob(family.executableName)) {
                if (!LuaFileUtil.matchesGlob(family.executableName, e))
                    continue;
            } else if (!e.equals(family.getPlatformExecutableName()))
                continue;

            return family;
        }
        return null;
    }

    @NotNull
    public String getInterpreterName() {
        return interpreterName;
    }

    @NotNull
    public String getExecutableName() {
        return executableName;
    }

    @NotNull
    public String getFamilyNameMatch() {
        return familyNameMatch;
    }

    @NotNull
    public BinaryType getBinaryType() {
        return binaryType;
    }

    @Nullable
    public String getArgExecCode() {
        return argExecCode;
    }

    @Nullable
    public String getArgLoadLib() {
        return argLoadLib;
    }
}
