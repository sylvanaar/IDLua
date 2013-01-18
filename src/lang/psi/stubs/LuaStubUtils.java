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

import com.intellij.openapi.util.Pair;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaTypedStub;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaPrimitiveType;
import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
import com.sylvanaar.idea.Lua.lang.psi.types.StubType;

import java.io.IOException;

/**
 * User: Dmitry.Krasilschikov
 * Date: 02.06.2009
 */
public class LuaStubUtils {
    public static void writeSubstitutableType(LuaType type, byte[] encoded, StubOutputStream dataStream) throws IOException {
        final boolean primitive = type instanceof LuaPrimitiveType;

        dataStream.writeBoolean(primitive);

        if (primitive) {
            dataStream.writeByte(((LuaPrimitiveType) type).getId());
        } else {
            assert encoded != null : "Invalid encoded type";
            dataStream.writeVarInt(encoded.length);
            dataStream.write(encoded);
        }
    }

    public static Pair<LuaType, byte[]> readSubstitutableType(StubInputStream dataStream) throws IOException {
        final boolean primitive = dataStream.readBoolean();
        LuaType type = null;
        byte[] bytes = null;


        if (primitive)
            type = LuaPrimitiveType.PRIMITIVE_TYPES[dataStream.readByte()];
        else {
            bytes = new byte[dataStream.readVarInt()];
            int len = dataStream.read(bytes, 0, bytes.length);
            assert len == bytes.length : "read wrong length";
        }
        return new Pair<LuaType, byte[]>(type, bytes);
    }

    public static LuaType GetStubOrPrimitiveType(LuaTypedStub stub) {
        final LuaType luaType = stub.getLuaType();
        if (luaType != null) return luaType;

        final byte[] encodedType = stub.getEncodedType();
        return encodedType == null ? LuaPrimitiveType.ANY : new StubType(encodedType);
    }
}
