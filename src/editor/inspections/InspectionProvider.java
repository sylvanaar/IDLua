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
import com.intellij.openapi.components.ApplicationComponent;
import com.sylvanaar.idea.Lua.editor.inspections.bugs.GlobalSelfInspection;
import com.sylvanaar.idea.Lua.editor.inspections.bugs.LuaDivideByZeroInspection;
import com.sylvanaar.idea.Lua.editor.inspections.bugs.UnbalancedAssignmentInspection;
import com.sylvanaar.idea.Lua.editor.inspections.metrics.LuaOverlyComplexMethodInspection;
import com.sylvanaar.idea.Lua.editor.inspections.metrics.LuaOverlyLongMethodInspection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 7:25:47 AM
 */
public class InspectionProvider implements InspectionToolProvider, ApplicationComponent {
    public Class[] getInspectionClasses() {
        return new Class[] {
                GlobalSelfInspection.class,
                UnbalancedAssignmentInspection.class,
                LuaDivideByZeroInspection.class,
               // AnonymousFunctionAssignmentInspection.class,
                LuaOverlyComplexMethodInspection.class,
                LuaOverlyLongMethodInspection.class,
              //  LuaUnreachableStatementInspection.class,
             //   StringConcatenationInLoopsInspection.class,
        };
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "LuaInspectionProvider";
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }
}
