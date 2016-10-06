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

package com.sylvanaar.idea.Lua.lang.psi.types;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 7/2/12
 * Time: 7:18 PM
 */
public class LuaPrimitiveTypeImpl implements LuaPrimitiveType {
    private static final long serialVersionUID = 2706840416961654130L;
    private final String name;
    private final String encoding;
    private final int    id;

    public LuaPrimitiveTypeImpl(@NotNull String name, @NotNull String encoding, int id) {
        this.name = name;
        this.encoding = encoding;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LuaPrimitiveTypeImpl that = (LuaPrimitiveTypeImpl) o;

        if (id != that.id) {
            return false;
        }
        if (!encoding.equals(that.encoding)) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31*result + encoding.hashCode();
        result = 31*result + id;
        return result;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override public String getEncodedAsString() {
        return encoding;
    }

    @Override public String encode(Map<LuaType, String> encodingContext) {
        return encoding;
    }

    @Override public String encodingResult(Map<LuaType, String> encodingContext, String encoded) {
        return encoding;
    }

    @Override public LuaType getFromEncodedString(byte... input) {
        throw new UnsupportedOperationException("Encoded primitive types not supported");
    }

    @Override public LuaType combineTypes(LuaType type1, LuaType type2) {
        return LuaTypeUtil.combineTypes(type1, type2);
    }

    @Override public String toString() {
        return name;
    }
}
