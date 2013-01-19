/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.sylvanaar.idea.Lua.lang.psi.stubs;

import com.intellij.openapi.util.*;
import com.intellij.psi.stubs.*;
import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.*;
import com.sylvanaar.idea.Lua.lang.psi.types.*;
import org.jetbrains.annotations.*;

import java.io.*;

/**
 * User: Dmitry.Krasilschikov
 * Date: 02.06.2009
 */
public class LuaStubUtils {
    public static void writeNullableString(StubOutputStream dataStream, @Nullable String typeText) throws IOException {
        dataStream.writeBoolean(typeText != null);
        if (typeText != null) {
            dataStream.writeUTFFast(typeText);
        }
    }

    @Nullable
    public static String readNullableString(StubInputStream dataStream) throws IOException {
        final boolean hasTypeText = dataStream.readBoolean();
        return hasTypeText ? dataStream.readUTFFast() : null;
    }

    public static void writeSubstitutableType(LuaType type, byte[] encoded, StubOutputStream dataStream) throws IOException {
        dataStream.writeBoolean(type instanceof LuaPrimativeType);

        if (type instanceof LuaPrimativeType) {
            dataStream.writeByte(((LuaPrimativeType) type).getId());
        } else {
            dataStream.writeVarInt(encoded.length);
            dataStream.write(encoded);
        }
    }

    public static Pair<LuaType, byte[]> readSubstitutableType(StubInputStream dataStream) throws IOException {
        final boolean primative = dataStream.readBoolean();
        LuaType type = null;
        byte[] bytes = null;


        if (primative)
            type = LuaPrimativeType.PRIMATIVE_TYPES[dataStream.readByte()];
        else {
            bytes = new byte[dataStream.readVarInt()];
            dataStream.read(bytes, 0, bytes.length);
        }
        return new Pair<LuaType, byte[]>(type, bytes);
    }

    public static LuaType GetStubOrPrimativeType(LuaTypedStub stub, LuaExpression psi) {
        final LuaType luaType = stub.getLuaType();
        if (luaType != null) return luaType;

        final byte[] encodedType = stub.getEncodedType();
        return encodedType == null ? LuaType.ANY : new StubType(encodedType);
    }
}
