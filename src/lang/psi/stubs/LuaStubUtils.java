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

    public static void writePrimativeTypeOrLength(LuaType type, byte[] encoded,
                                                  StubOutputStream dataStream) throws IOException {
        if (type instanceof LuaPrimativeType) {
            dataStream.writeShort(((LuaPrimativeType) type).getId());
        } else {
            dataStream.writeShort(encoded.length);
            dataStream.write(encoded);
        }
    }

    public static LuaType readePrimativeType(StubInputStream dataStream, int len) throws IOException {
        if (len < 0) ((SerializationManagerEx) SerializationManagerEx.getInstance()).repairNameStorage();
        final LuaType[] types = LuaPrimativeType.PRIMATIVE_TYPES;
        if (len >= types.length)
            return null;

        return types[len];
    }

    public static byte[] readEncodedType(StubInputStream dataStream, int len) throws IOException {
        byte[] typedata = new byte[len];
        dataStream.read(typedata, 0, len);
        return typedata;
    }

    public static LuaType GetStubOrPrimativeType(LuaTypedStub stub, LuaExpression psi) {
        final LuaType luaType = stub.getLuaType();
        return luaType != null ? luaType : new StubType(stub.getEncodedType());
    }

    public static class ExtractVariableType {
        private StubInputStream dataStream;
        private LuaType         type;
        private byte[]          typedata;

        public ExtractVariableType(StubInputStream dataStream) {this.dataStream = dataStream;}

        public LuaType getType() {
            return type;
        }

        public byte[] getTypedata() {
            return typedata;
        }

        public ExtractVariableType invoke() throws IOException {
            int len = dataStream.readShort();
            type = readePrimativeType(dataStream, len);
            typedata = null;
            if (type != null) typedata = readEncodedType(dataStream, len);
            return this;
        }
    }
}
