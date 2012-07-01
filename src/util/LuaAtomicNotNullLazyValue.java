/*
 * Copyright 2012 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.util;

import com.intellij.openapi.util.*;
import org.jetbrains.annotations.*;

public abstract class LuaAtomicNotNullLazyValue<T> extends NotNullLazyValue<T> {

    private volatile T myValue;

    @NotNull
    public final T getValue() {
        T value = myValue;
        if (value != null) {
            return value;
        }
        synchronized (this) {
            value = myValue;
            if (value == null) {
                myValue = value = compute();
            }
        }
        return value;
    }


    public void drop() {
        synchronized (this) {
            myValue = null;
        }
    }
}