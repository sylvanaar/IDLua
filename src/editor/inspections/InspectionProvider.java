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

package com.sylvanaar.idea.Lua.editor.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.sylvanaar.idea.Lua.editor.inspections.bugs.*;
import com.sylvanaar.idea.Lua.editor.inspections.metrics.LuaOverlyComplexMethodInspection;
import com.sylvanaar.idea.Lua.editor.inspections.metrics.LuaOverlyLongMethodInspection;
import com.sylvanaar.idea.Lua.editor.inspections.performance.RedundantInitializationInspection;
import com.sylvanaar.idea.Lua.editor.inspections.performance.StringConcatenationInLoopsInspection;
import com.sylvanaar.idea.Lua.editor.inspections.unassignedVariable.UnassignedVariableAccessInspection;
import com.sylvanaar.idea.Lua.editor.inspections.usage.UnusedDefInspection;
import com.sylvanaar.idea.Lua.editor.inspections.validity.LuaUnreachableStatementInspection;
import com.sylvanaar.idea.Lua.luaj.PlatformLuaJavaLib;
import com.sylvanaar.idea.Lua.util.LuaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 7:25:47 AM
 */
public class InspectionProvider implements InspectionToolProvider {
    public Class[] getInspectionClasses() {

        List<?> classes = Arrays.asList(
                ParameterSelfInspection.class,
                GlobalSelfInspection.class,
                UnbalancedAssignmentInspection.class,
                LuaDivideByZeroInspection.class,
                LuaOverlyComplexMethodInspection.class,
                LuaOverlyLongMethodInspection.class,
                ArrayElementZeroInspection.class,
                LuaUnreachableStatementInspection.class,
                StringConcatenationInLoopsInspection.class,
                RedundantInitializationInspection.class,
                GlobalCreationOutsideOfMainChunk.class,
                UnassignedVariableAccessInspection.class,
                UnusedDefInspection.class
        );


        final VirtualFile luaIntenttionsDirectory = getLuaIntenttionsDirectory();
        if (luaIntenttionsDirectory != null) {
            LuaFileUtil.iterateLuaFilesRecursively(luaIntenttionsDirectory, new ContentIterator() {
                private final List<Class> myClasses = new ArrayList<Class>();
                @NotNull
                public List<Class> getClasses() {
                    return myClasses;
                }

                @Override
                public boolean processFile(VirtualFile fileOrDir) {
                    if (fileOrDir.isDirectory()) return true;

                    Globals globals = JsePlatform.standardGlobals();
                    globals.load(new PlatformLuaJavaLib());
                    LuaValue chunk = globals.loadFile(fileOrDir.getPath());

                    try {
                        final LuaValue result = chunk.call();

                        final Class<? extends LuaValue> aClass = result.getClass();
                        myClasses.add(aClass);
                    } catch (Throwable t) {
                        int i = 1;
                    }
                    return true;
                }
            });
        }

        return (Class[]) ArrayUtil.toObjectArray(classes);
    }

    public static VirtualFile getLuaIntenttionsDirectory() {
        VirtualFile dir = LuaFileUtil.getPluginVirtualDirectory();

        if (dir != null) dir = dir.findChild("luainspections");

        if (dir != null) return dir;

        return null;
    }
}
