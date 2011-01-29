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
package com.sylvanaar.idea.Lua.lang.documentor;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;
import java.util.Set;

public class MethodSignatureBundle {
    private static final ResourceBundle OUR_BUNDLE =
            ResourceBundle.getBundle("com.sylvanaar.idea.Lua.lang.documentor.MethodSignatureBundle");

    private MethodSignatureBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = "com.sylvanaar.idea.Lua.lang.documentor.MethodSignatureBundle")
                                String key,
                                 Object... params) {
        if (OUR_BUNDLE.keySet().contains(key))
            return CommonBundle.message(OUR_BUNDLE, key, params);

        return  null;
    }


    public static Set<String> getAllKeys() {
        return OUR_BUNDLE.keySet();
    }
}
